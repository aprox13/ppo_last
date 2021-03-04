package ru.ifkbhit.ppo.akka.actor

import akka.actor.{Actor, Cancellable}
import ru.ifkbhit.ppo.common.Logging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

trait BaseActor extends Actor with Logging {

  override def postStop(): Unit =
    log.info(s"Actor $self was stopped")

  override def unhandled(msg: Any): Unit =
    log.warn(s"Unhandled message $msg")

  protected def process: Receive

  override def receive: Receive = {
    case x if process.isDefinedAt(x) =>
      process(x)

    case BaseActor.StopActor =>
      log.info("Base stop processing")
      context.children.foreach(_ ! BaseActor.StopActor)
      context.stop(self)

    case BaseActor.Timeout =>
      log.error(s"Timeout in $self")
      context.stop(self)
  }

  protected def scheduleTimeout(duration: FiniteDuration)(implicit ec: ExecutionContext): Cancellable =
    context.system.scheduler.scheduleOnce(duration)(self ! BaseActor.Timeout)

}

object BaseActor {

  case object StopActor
  case object Timeout
}
