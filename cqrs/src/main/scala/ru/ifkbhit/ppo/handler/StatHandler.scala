package ru.ifkbhit.ppo.handler

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ru.ifkbhit.ppo.BaseDirectives
import ru.ifkbhit.ppo.common.handler.JsonAnsweredHandler
import ru.ifkbhit.ppo.manager.StatManager
import ru.ifkbhit.ppo.model.stat.{PerDayReportQuery, StatQuery}

import scala.concurrent.ExecutionContext

class StatHandler(manager: StatManager)(implicit val ec: ExecutionContext) extends JsonAnsweredHandler with BaseDirectives {

  private def perDay: Route =
    (get &
      pathPrefix("stats") &
      Query &
      UserInPath &
      pathSuffix("daily")) { userId =>
      completeResponse(manager.getPerDayPasses(PerDayReportQuery(userId)))
    }

  private def stats: Route =
    (post &
      path("stats") &
      Query &
      entity(as[StatQuery])) { statQuery =>
      completeResponse(manager.getStat(statQuery))
    }

  override def route: Route = perDay ~ stats
}
