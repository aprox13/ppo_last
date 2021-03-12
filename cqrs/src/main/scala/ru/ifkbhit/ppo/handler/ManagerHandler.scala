package ru.ifkbhit.ppo.handler

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ru.ifkbhit.ppo.BaseDirectives
import ru.ifkbhit.ppo.common.handler.JsonAnsweredHandler
import ru.ifkbhit.ppo.manager.ManagersManager
import ru.ifkbhit.ppo.model.manager.UserPayload

import scala.concurrent.ExecutionContext

class ManagerHandler(manager: ManagersManager)(implicit val ec: ExecutionContext) extends JsonAnsweredHandler with BaseDirectives {

  private def getUserRoute: Route =
    (get & pathPrefix("manager")
      & Query
      & UserInPath) { userId =>
      completeResponse(manager.getUser(userId))
    }

  private def renewPass: Route =
    (put & pathPrefix("manager")
      & Command
      & UserInPath
      & parameters('days.as[Int])) {
      case (user, days) =>
        completeResponse(manager.renewPass(user, days))
    }

  private def addUser: Route =
    (post & pathPrefix("manager")
      & Command
      & pathPrefix("user" / "add")
      & entity(as[UserPayload])) {
      payload =>
        completeResponse(manager.addUser(payload))
    }

  override def route: Route = getUserRoute ~ renewPass ~ addUser
}
