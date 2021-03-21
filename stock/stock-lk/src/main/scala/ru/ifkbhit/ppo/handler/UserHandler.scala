package ru.ifkbhit.ppo.handler

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ru.ifkbhit.ppo.common.handler.{BaseDirectives, JsonAnsweredHandler}
import ru.ifkbhit.ppo.manager.UserManager
import ru.ifkbhit.ppo.model.Money
import ru.ifkbhit.ppo.request.UserCreateRequest

import scala.concurrent.ExecutionContext

class UserHandler(userManager: UserManager)(implicit val ec: ExecutionContext)
  extends JsonAnsweredHandler
    with BaseDirectives {
  override def route: Route =
    UserInPath { userId =>
      get {
        completeResponse(userManager.getUserInfo(userId))
      } ~ (put & path("money" / "add") & entity(as[Money])) { money =>
        completeResponse(userManager.addMoney(userId, money))
      }
    } ~ (put & path("user" / "add") & entity(as[UserCreateRequest])) { request =>
      completeResponse(userManager.addUser(request))
    }

}
