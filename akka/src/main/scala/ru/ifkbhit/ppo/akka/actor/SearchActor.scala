package ru.ifkbhit.ppo.akka.actor

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{Callable, FutureTask}

import ru.ifkbhit.ppo.akka.manager.SearchManager
import ru.ifkbhit.ppo.akka.model.SearchResponse
import ru.ifkbhit.ppo.common.model.response.Response
import scala.util.Try


class SearchActor(searchManager: SearchManager) extends BaseActor {

  import SearchActor._

  protected val task: AtomicReference[FutureTask[Unit]] = new AtomicReference[FutureTask[Unit]]()

  override protected def process: Receive = {
    case SearchRequestMessage(request, engine) =>
      log.info(s"Processing '$request' to $engine")

      task.set(new FutureTask[Unit](
        new Callable[Unit] {
          override def call(): Unit = {

            val response = Try {
              log.info(s"Requesting $engine")
              val r = searchManager.searchFrom(engine, request)
              log.info(s"Here $engine")
              r
            }.map(successful(_, engine))
              .recover {
                case t => failed(engine, t)
              }.get

            log.info(s"Got response $response")

            self ! response
          }
        }
      )
      )

      Option(task.get).foreach(context.dispatcher.execute)


    case response: SearchResponseMessage =>
      context.parent ! response
      self ! BaseActor.StopActor

    case BaseActor.StopActor =>
      Option(task.get()) match {
        case Some(t) =>
          log.info(s"Cancelling task ${t.cancel(true)}")
        case None =>
          log.info("Task already done")

      }
      context.stop(self)
  }
}

object SearchActor {

  case class SearchRequestMessage(request: String, engine: String)

  case class SearchResponseMessage(response: Response[SearchResponse], engine: String)

  case class FailedSearch(engine: String, exception: Throwable)

  private def successful(response: Response[SearchResponse], engine: String) =
    SearchResponseMessage(response, engine)

  private def failed(engine: String, throwable: Throwable) =
    SearchResponseMessage(Response.FailedResponse(s"Error while requesting $engine: ${throwable.getCause}"), engine)
}
