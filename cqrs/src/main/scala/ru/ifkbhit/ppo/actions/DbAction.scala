package ru.ifkbhit.ppo.actions

import java.sql.Connection

import ru.ifkbhit.ppo.actions.DbAction.EmptyActionResult
import ru.ifkbhit.ppo.util.SqlUtils.ConnectionOps

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class DbAction[T](f: Connection => T) {

  def run(connection: Connection)(implicit ec: ExecutionContext): Future[T] =
    Future(f(connection))

  def transactional(connection: Connection)(implicit ex: ExecutionContext): Future[T] =
    connection.transactional(f)

  def transactionalBlocked(connection: Connection): T =
    connection.transactionalBlocked(f)

  def map[R](mapper: T => R): DbAction[R] = DbAction { conn =>
    mapper(f(conn))
  }

  def flatMap[R](mapper: T => DbAction[R]): DbAction[R] = DbAction { conn =>
    mapper(f(conn)).f(conn)
  }

  def withFilter(filter: T => Boolean): DbAction[T] = DbAction { conn =>
    val t = f(conn)

    if (filter(t)) {
      t
    } else {
      throw EmptyActionResult
    }
  }

  def recover(pf: PartialFunction[Throwable, T]): DbAction[T] = DbAction { conn =>
    Try(f(conn))
      .recover(pf)
      .get
  }
}

object DbAction {

  def None[T]: DbAction[Option[T]] = DbAction[Option[T]](_ => scala.None)

  def doIf[T](test: Boolean)(f: => DbAction[Option[T]]): DbAction[Option[T]] =
    if (test) f else None

  def failed[T](throwable: Throwable): DbAction[T] = DbAction(_ => throw throwable)

  def success[T](e: T): DbAction[T] = DbAction(_ => e)

  case object EmptyActionResult extends RuntimeException("Empty result!")

}