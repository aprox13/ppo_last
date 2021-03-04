package ru.ifkbhit.ppo.akka.actor

import java.util.concurrent.atomic.AtomicReference

import akka.actor.{ActorRef, Cancellable, Props}
import ru.ifkbhit.ppo.akka.manager.SearchManager
import ru.ifkbhit.ppo.akka.model.{ApiResponse, SearchResponse}
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.model.response.Response
import ru.ifkbhit.ppo.common.utils.AtomicRefOps._

import scala.concurrent.duration.FiniteDuration

class ApiActor(searchManager: SearchManager, collectingTimeout: FiniteDuration) extends BaseActor {

  import ApiActor._

  private val apiContext: AtomicReference[ApiContext] = new AtomicReference[ApiContext]()
  private val scheduleTimeout: AtomicReference[Cancellable] = new AtomicReference[Cancellable]()

  override protected def process: Receive = {
    case ApiRequestMessage(request) =>

      val apiContext = searchManager.engines
        .foldLeft(ApiContext.empty(sender())) {
          case (apiCtx, engine) =>
            val child = context.actorOf(Props(classOf[SearchActor], searchManager), s"engine-$engine")

            child ! SearchActor.SearchRequestMessage(request, engine)
            log.info(s"Sent request to $engine")

            apiCtx.update(engine, TimeoutResponse)
        }

      this.apiContext.set(apiContext.copy(processed = 0))

      scheduleTimeout.set(
        scheduleTimeout(collectingTimeout)(context.system.dispatcher)
      )
    case SearchActor.SearchResponseMessage(response, engine)
      if apiContext.exists(_.requestsLeft == 1) =>
      apiContext.foreach(_.update(engine, response).replyToMainSender())
      releaseTimeout()
      self ! BaseActor.StopActor


    case SearchActor.SearchResponseMessage(response, engine) =>
      apiContext.opt.map(_.update(engine, response))
        .foreach(apiContext.set)

    case BaseActor.Timeout =>
      log.info("Timeout!")
      apiContext.foreach(_.replyToMainSender())

      self ! BaseActor.StopActor
  }

  private def releaseTimeout(): Unit = {
    scheduleTimeout.opt.foreach {
      x => log.info(s"Release timeout schedule ${x.cancel()}")
    }
  }
}

object ApiActor extends Logging {
  case class ApiRequestMessage(request: String)

  private val TimeoutResponse: Response[SearchResponse] = Response.FailedResponse("Timeout!")

  private case class ApiContext(result: Map[String, Response[SearchResponse]], processed: Int, mainSender: ActorRef) {

    def update(engine: String, response: Response[SearchResponse]): ApiContext =
      this.copy(
       result = result.updated(engine, response),
       processed = processed + 1
      )

    def requestsLeft: Int = result.size - processed

    def replyToMainSender(): Unit =
      mainSender ! Response.SuccessfulResponse(ApiResponse(result))
  }

  private object ApiContext {
    def empty(sender: ActorRef): ApiContext = ApiContext(Map.empty, 0, sender)
  }

}

