package ru.ifkbhit.ppo.akka.actor

import ru.ifkbhit.ppo.akka.manager.SearchManager
import ru.ifkbhit.ppo.common.model.response.Response

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


class SearchActor(searchManager: SearchManager)(implicit ec: ExecutionContext) extends BaseActor {

  import SearchActor._

  override protected def process: Receive = {
    case SearchRequestMessage(request, engine) =>
      searchManager.searchFrom(engine, request).onComplete {
        case Failure(exception) =>
          self ! SearchResponseMessage(Response.fromThrowable(exception), engine)
        case Success(value) =>
          self ! SearchResponseMessage(value, engine)
      }


    case response: SearchResponseMessage =>
      context.parent ! response
  }
}

object SearchActor {

  case class SearchRequestMessage(request: String, engine: String)

  case class SearchResponseMessage(response: Response, engine: String)

}
