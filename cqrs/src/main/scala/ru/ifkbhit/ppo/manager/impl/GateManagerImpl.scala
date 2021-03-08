package ru.ifkbhit.ppo.manager.impl

import java.sql.Connection

import ru.ifkbhit.ppo.actions.{EventActions, ManagerActions}
import ru.ifkbhit.ppo.common.model.response.Response
import ru.ifkbhit.ppo.dao.DbAction
import ru.ifkbhit.ppo.dao.DbAction.EmptyActionResult
import ru.ifkbhit.ppo.manager.GateManager
import ru.ifkbhit.ppo.model.event.EventType
import spray.json.DefaultJsonProtocol.StringJsonFormat
import spray.json.JsObject

import scala.concurrent.{ExecutionContext, Future}

class GateManagerImpl(database: Connection, events: EventActions, managers: ManagerActions)(implicit ec: ExecutionContext) extends GateManager {

  override def enter(userId: Long): Future[Response] =
    (for {
      user <- managers.getUser(userId)
      if user.isPassActive
      lastEvent <- events.getLastEventOf(Seq(EventType.UserExit, EventType.UserEntered), Some(userId))
      _ <- if (lastEvent.forall(_.eventType == EventType.UserExit)) {
        events.insertOne(EventType.UserEntered, JsObject(), userId)
      } else {
        DbAction.failed(new RuntimeException("User already enter"))
      }
    } yield ())
      .transactional(database)
      .map(_ => Response.success("User entered"))
      .recover[Response] {
        case EmptyActionResult =>
          Response.failed("No pass found for user")
        case e =>
          Response.fromThrowable(e)
      }

  override def exit(userId: Long): Future[Response] =
    (for {
      _ <- managers.getUser(userId)
      lastEvent <- events.getLastEventOf(Seq(EventType.UserExit, EventType.UserEntered), Some(userId))
      _ <- if (lastEvent.exists(_.eventType == EventType.UserEntered)) {
        events.insertOne(EventType.UserExit, JsObject(), userId)
      } else {
        DbAction.failed(new RuntimeException("User already exit"))
      }
    } yield ())
      .transactional(database)
      .map(_ => Response.success("User exit"))
      .recover[Response] {
        case e =>
          Response.fromThrowable(e)
      }
}
