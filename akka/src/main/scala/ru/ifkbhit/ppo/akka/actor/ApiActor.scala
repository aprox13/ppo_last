package ru.ifkbhit.ppo.akka.actor

import java.util.concurrent.atomic.AtomicReference

import akka.actor.{Props, ReceiveTimeout}
import ru.ifkbhit.ppo.akka.manager.ApiManager.ApiResponse
import ru.ifkbhit.ppo.akka.manager.SearchManager
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.model.response.Response
import ru.ifkbhit.ppo.common.utils.AtomicRefOps._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Promise}
import scala.util.Try

class ApiActor(
  searchManager: SearchManager,
  collectingTimeout: FiniteDuration,
  promise: Promise[ApiResponse]
)(implicit ec: ExecutionContext) extends BaseActor {

  import ApiActor._

  private val apiContext: AtomicReference[ApiContext] = new AtomicReference[ApiContext]()

  private def updateContext(engine: String, response: Response): Unit =
    apiContext.foreach { ctx =>
      apiContext.set(ctx.update(engine, response))
    }

  private def reply(): Unit =
    if (!promise.isCompleted)
      promise.complete(
        Try(apiContext.opt.map(_.result).getOrElse(throw new RuntimeException("No replies found")))
      )

  override protected def process: Receive = {
    case ApiRequestMessage(request) =>
      this.apiContext.set(ApiContext.empty)

      searchManager.engines
        .foreach(updateContext(_, SearcherTimeoutResponse))

      searchManager.engines
        .foreach { engine =>
          val child = context.actorOf(Props(classOf[SearchActor], searchManager, ec), s"engine-$engine")

          child ! SearchActor.SearchRequestMessage(request, engine)
          log.info(s"Sent request to $engine")
        }

      context.setReceiveTimeout(collectingTimeout)

    case SearchActor.SearchResponseMessage(response, engine) =>
      updateContext(engine, response)

      if (apiContext.exists(_.isFull)) {
        reply()
        self ! BaseActor.StopActor
      }

    case _: ReceiveTimeout =>
      reply()

  }
}

object ApiActor extends Logging {

  case class ApiRequestMessage(request: String)

  val SearcherTimeoutResponse: Response = Response.failed("Searcher timeout")

  private case class ApiContext(result: ApiResponse, processed: Int) {

    def update(engine: String, response: Response): ApiContext =
      this.copy(
        result = result.updated(engine, response),
        processed = processed + 1
      )

    def isFull: Boolean = result.size == processed
  }

  private object ApiContext {
    def empty: ApiContext = ApiContext(Map.empty, 0)
  }

}

