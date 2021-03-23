package ru.ifkbhit.ppo.future

import org.scalatest.exceptions.TestFailedException
import org.scalatest.matchers.{BeMatcher, MatchResult}
import ru.ifkbhit.ppo.future.FutureMatcher._

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

trait FutureMatcher {
  def successFuture[T]: BeMatcher[Future[T]] = new SuccessFutureWord[T](None)

  def successFuture[T](e: T): BeMatcher[Future[T]] = new SuccessFutureWord[T](Some(e))

  val failureFuture: BeMatcher[Nothing] = new FailureFutureWord(None)

  def failureFuture[T](throwable: Throwable): BeMatcher[Future[T]] = new FailureFutureWord[T](Option(throwable))
}

object FutureMatcher {

  implicit class FutureOps[T](val future: Future[T]) extends AnyVal {

    import scala.concurrent.duration._

    def asTry: Try[T] = Try(Await.result(future, 1.second)) recover {
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