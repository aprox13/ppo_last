package ru.ifkbhit.ppo.utils

import java.sql.Connection
import java.util.concurrent.atomic.AtomicReference

import org.joda.time.DateTime
import org.scalamock.matchers.MockParameter
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.exceptions.TestFailedException
import org.scalatest.matchers.{BeMatcher, MatchResult}
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import ru.ifkbhit.ppo.actions.EventActions
import ru.ifkbhit.ppo.model.event.Event
import ru.ifkbhit.ppo.utils.BaseManagerSpec.{FailureFutureWord, SuccessFutureWord}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


trait BaseManagerSpec extends WordSpec with Matchers with MockFactory with BeforeAndAfter {

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

  def successFuture[T]: BeMatcher[Future[T]] = new SuccessFutureWord[T](None)

  def successFuture[T](e: T): BeMatcher[Future[T]] = new SuccessFutureWord[T](Some(e))

  val failureFuture: BeMatcher[Nothing] = new FailureFutureWord(None)

  def failureFuture[T](throwable: Throwable): BeMatcher[Future[T]] = new FailureFutureWord[T](Option(throwable))
}

object BaseManagerSpec {

  implicit class FutureOps[T](val future: Future[T]) extends AnyVal {

    def asTry: Try[T] = Try(future.futureValue) recover {
      case x: TestFailedException if x.cause.isDefined =>
        throw x.cause.get
    }
  }

  class SuccessFutureWord[T](inner: Option[T]) extends BeMatcher[Future[T]] {
    override def apply(left: Future[T]): MatchResult = {
      val res = left.asTry

      res match {
        case x@Failure(_) =>
          MatchResult(
            matches = false,
            s"Success expected${inner.map(i => s" with entry $i").getOrElse("")}, but found $x",
            s"Success"
          )
        case x@Success(value) =>
          MatchResult(
            inner.forall(_ == value),
            s"Success expected${inner.map(i => s" with entry $i").getOrElse("")}, but found $x",
            s"Found $x"
          )
      }


    }
  }

  private class FailureFutureWord[T](inner: Option[Throwable]) extends BeMatcher[Future[T]] {
    override def apply(left: Future[T]): MatchResult = {
      val res = left.asTry

      res match {
        case x@Failure(e) =>
          MatchResult(
            inner.forall(i => i == e),
            s"Failure expected${inner.map(i => s" with exception $i").getOrElse("")}, but found $x",
            s"Failure"
          )
        case x@Success(_) =>
          MatchResult(
            matches = false,
            s"Failure expected${inner.map(i => s" with exception $i").getOrElse("")}, but found $x",
            s"Found $x"
          )
      }


    }
  }

}

