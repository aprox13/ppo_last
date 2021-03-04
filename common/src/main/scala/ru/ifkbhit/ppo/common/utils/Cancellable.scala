package ru.ifkbhit.ppo.common.utils

import java.util.concurrent.{Callable, FutureTask}

import scala.concurrent.{ExecutionContext, Future}

class Cancellable[T](executionContext: ExecutionContext, todo: => T) {

  private val jf: FutureTask[T] = new FutureTask[T](
    new Callable[T] {
      override def call(): T = todo
    }
  )

  executionContext.execute(jf)

  implicit val _: ExecutionContext = executionContext

  val future: Future[T] = Future {
      jf.get
  }(executionContext)

  def cancel(): Unit = jf.cancel(true)

}

object Cancellable {
  def apply[T](todo: => T)(implicit executionContext: ExecutionContext): Cancellable[T] =
    new Cancellable[T](executionContext, todo)
}
