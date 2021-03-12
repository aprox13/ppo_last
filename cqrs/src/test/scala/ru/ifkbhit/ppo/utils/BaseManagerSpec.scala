package ru.ifkbhit.ppo.utils

import java.sql.Connection
import java.util.concurrent.atomic.AtomicReference

import org.joda.time.DateTime
import org.scalamock.matchers.MockParameter
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import ru.ifkbhit.ppo.actions.EventActions
import ru.ifkbhit.ppo.model.event.Event

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext


class BaseManagerSpec extends WordSpec with Matchers with MockFactory with BeforeAndAfter {

  protected val database: AtomicReference[ArrayBuffer[Event]] = new AtomicReference[ArrayBuffer[Event]](ArrayBuffer())
  protected var connection: Connection = _
  protected implicit val timer: SettableTimeProvider = SettableTimeProvider
  protected val eventActions: EventActions = new MemoryEventActions(database)

  protected implicit val ec: ExecutionContext = new ExecutionContext {
    override def execute(runnable: Runnable): Unit =
      runnable.run()

    override def reportFailure(cause: Throwable): Unit =
      throw cause
  }

  protected def transactionShouldRollback(): Unit =
    toMockFunction0(connection.rollback).expects()
      .once()
      .onCall(_ => ())

  protected def transactionShouldStart(): Unit =
    (connection.setAutoCommit _).expects(new MockParameter[Boolean](false))
      .once()
      .onCall(_ => ())

  protected def transactionShouldComplete(): Unit =
    toMockFunction0(connection.commit)
      .expects()
      .once()
      .onCall(_ => ())

  protected def withFailedTransaction[T](action: => T): T = {
    transactionShouldStart()
    transactionShouldRollback()

    action
  }

  protected def withSuccessTransaction[T](action: => T): T = {
    transactionShouldStart()
    transactionShouldComplete()

    action
  }

  before {
    database.set(ArrayBuffer())
    timer.setNow(DateTime.now())
    connection = mock[Connection]
  }

}

