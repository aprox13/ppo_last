package ru.ifkbhit.ppo.handler

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ru.ifkbhit.ppo.BaseDirectives
import ru.ifkbhit.ppo.common.handler.JsonAnsweredHandler
import ru.ifkbhit.ppo.manager.GateManager

class GateHandler(gateManager: GateManager) extends JsonAnsweredHandler with BaseDirectives {


  private def enterRoute: Route =
    (get & pathPrefix("gate") & Command & UserInPath & pathPrefix("enter")) { user =>
      complete(gateManager.enter(user))
    }

  private def exitRoute: Route =
    (get & pathPrefix("gate") & Command & UserInPath & pathPrefix("exit")) { user =>
      complete(gateManager.exit(user))
    }

  override def route: Route =
    enterRoute ~ exitRoute

}
