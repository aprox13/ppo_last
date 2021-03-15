package ru.ifkbhit.ppo.akka.actor

import akka.actor.Actor
import ru.ifkbhit.ppo.common.Logging

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
  }
}

object BaseActor {

  case object StopActor
}
