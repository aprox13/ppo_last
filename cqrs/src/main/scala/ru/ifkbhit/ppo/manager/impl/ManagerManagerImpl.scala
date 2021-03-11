package ru.ifkbhit.ppo.manager.impl

import java.sql.Connection

import ru.ifkbhit.ppo.actions._
import ru.ifkbhit.ppo.common.model.response.Response
import ru.ifkbhit.ppo.common.utils.MapOps._
import ru.ifkbhit.ppo.manager.ManagersManager
import ru.ifkbhit.ppo.model.event.EventType
import ru.ifkbhit.ppo.model.manager.{RenewPassPayload, UserPayload, UserResult}
import ru.ifkbhit.ppo.util.TimeProvider
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

class ManagerManagerImpl(
  database: Connection,
  events: EventActions,
  managerEvents: ManagerActions
)(
  implicit ec: ExecutionContext,
  timeProvider: TimeProvider
) extends ManagersManager {

  import UserPayload._

  override def getUser(userId: Long): Future[Response] = {
    (for {
      user <- managerEvents.getUserPayload(userId)
      renew <- events.getLastEvent(EventType.RenewPass, Some(userId))
    } yield UserResult.build(userId, user, renew.map(_.payloadAs[RenewPassPayload])))
      .transactional(database)
      .map(Response.success[UserResult])
      .recover {
        case e =>
          Response.fromThrowable(e)
      }
  }

  override def addUser(payload: UserPayload): Future[Response] = {
    (for {
      newId <- managerEvents.getNextUserId
      event <- events.insertAndReturn(EventType.CreateUser, payload.toJson(UserPayload.format), newId)
    } yield UserResult.build(newId, event.payloadAs[UserPayload], None))
      .transactional(database)
      .map(Response.success[UserResult])
      .recover {
        case e => Response.fromThrowable(e)
      }
  }

  override def renewPass(userId: Long, days: Int): Future[Response] = {
    if (days > 0) {
      (for {
        _ <- managerEvents.getUserPayload(userId)
        lastRenew <- events.getLastEvent(EventType.RenewPass, Some(userId))
        currentExpire = lastRenew.map(_.payloadAs[RenewPassPayload])
        newExpire = makeRenew(currentExpire, days)
        _ <- events.insertOne(EventType.RenewPass, newExpire.toJson, userId)
      } yield newExpire)
        .transactional(database)
        .map(Response.success[RenewPassPayload])
        .recover {
          case e =>
            Response.fromThrowable(e)
        }
    } else {
      Future.successful(Response.failed("Required positive days", 400))
    }
  }


  private def makeRenew(payloadOpt: Option[RenewPassPayload], days: Int): RenewPassPayload = {
    val today = timeProvider.now().withTimeAtStartOfDay()

    val payload = payloadOpt.getOrElse(RenewPassPayload.today)

    if (payload.expireAt.isBefore(today)) {
      RenewPassPayload(expireAt = today.plusDays(days))
    } else {
      payload.applyTransform(_.copy(expireAt = payload.expireAt.plusDays(days)))
    }
  }
}
