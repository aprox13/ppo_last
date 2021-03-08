package ru.ifkbhit.ppo.actions

import ru.ifkbhit.ppo.dao.DbAction
import ru.ifkbhit.ppo.model.event.EventType
import ru.ifkbhit.ppo.model.manager.{RenewPassPayload, UserPayload, UserResult}

trait ManagerActions {
  def getUserPayload(userId: Long): DbAction[UserPayload]

  def getNextUserId: DbAction[Long]

  def getUser(userId: Long): DbAction[UserResult]

  case class UserNotFound(id: Long) extends RuntimeException(s"User not found: id=$id")

}

class DefaultManagerActions(eventActions: EventActions) extends ManagerActions {

  def getUserPayload(userId: Long): DbAction[UserPayload] =
    for {
      userEvent <- eventActions.getLastEvent(EventType.CreateUser, Some(userId))
      result <- if (userEvent.isEmpty) {
        DbAction.failed(UserNotFound(userId))
      } else {
        DbAction.success(userEvent.map(_.payloadAs[UserPayload]).get)
      }
    } yield result

  def getNextUserId: DbAction[Long] =
    for {
      eventOpt <- eventActions.getLastEvent(EventType.CreateUser, None)
    } yield eventOpt.map(_.aggregateId).getOrElse(0L) + 1L

  def getUser(userId: Long): DbAction[UserResult] =
    for {
      user <- getUserPayload(userId)
      renew <- eventActions.getLastEvent(EventType.RenewPass, Some(userId))
    } yield UserResult.build(user, renew.map(_.payloadAs[RenewPassPayload]))
}