package ru.ifkbhit.ppo.manager.impl

import java.sql.Connection

import ru.ifkbhit.ppo.actions.DbAction.EmptyActionResult
import ru.ifkbhit.ppo.actions.{DbAction, EventActions, ManagerActions}
import ru.ifkbhit.ppo.manager.GateManager
import ru.ifkbhit.ppo.manager.GateManager._
import ru.ifkbhit.ppo.model.event.EventType
import ru.ifkbhit.ppo.model.gate.{UserEnterCommand, UserExitCommand}
import spray.json.JsObject

import scala.concurrent.{ExecutionContext, Future}

class GateManagerImpl(database: Connection, events: EventActions, managers: ManagerActions)(implicit ec: ExecutionContext) extends GateManager {

  override def enter(cmd: UserEnterCommand): Future[String] = {
    val userId = cmd.userId
    (for {
      user <- managers.getUser(userId)
      if user.isPassActive
      lastEvent <- events.getLastEventOf(Seq(EventType.UserExit, EventType.UserEntered), Some(userId))
      _ <- if (lastEvent.forall(_.eventType == EventType.UserExit)) {
        events.insertOne(EventType.UserEntered, JsObject(), userId)
      } else {
        DbAction.failed(UserAlreadyEnter)
      }
    } yield ())
      .transactional(database)
      .map(_ => "User entered")
      .recover {
        case EmptyActionResult =>
          throw UserHasNoPass
      }
  }

  override def exit(userExitCommand: UserExitCommand): Future[String] = {
    val userId: Long = userExitCommand.userId
    (for {
      _ <- managers.getUser(userId)
      lastEvent <- events.getLastEventOf(Seq(EventType.UserExit, EventType.UserEntered), Some(userId))
      _ <- if (lastEvent.exists(_.eventType == EventType.UserEntered)) {
        events.insertOne(EventType.UserExit, JsObject(), userId)
      } else {
        DbAction.failed {
          throw if (lastEvent.isEmpty) {
            UserNotEnterYet
          } else {
            UserAlreadyExit
          }
        }
      }
    } yield ())
      .transactional(database)
      .map(_ => "User exit")
  }
}
