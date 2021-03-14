package ru.ifkbhit.ppo.util

import java.sql.{Connection, ResultSet}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object SqlUtils {

  implicit class ConnectionOps(val connection: Connection) extends AnyVal {

    def transactionalBlocked[T](
      action: Connection => T
    ): T = Try {
      connection.setAutoCommit(false)
      val result = action(connection)
      connection.commit()
      result
    } match {
      case Failure(exception) =>
        connection.rollback()
        throw exception
      case Success(value) =>
        value
    }

    def transactional[T](
      action: Connection => T
    )(
      implicit ec: ExecutionContext
    ): Future[T] = Future(transactionalBlocked(action))
  }

  def parseResultSet[T](rs: ResultSet)(f: ResultSet => T): Seq[T] = {
    val res = ArrayBuffer[T]()

    while (rs.next()) {
      res += f(rs)
    }

    res.result()
  }

}
