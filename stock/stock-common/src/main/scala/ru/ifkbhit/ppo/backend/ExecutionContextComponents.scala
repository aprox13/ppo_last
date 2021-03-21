package ru.ifkbhit.ppo.backend

import scala.concurrent.ExecutionContext

trait ExecutionContextComponents {

  implicit def ec: ExecutionContext
}

trait DefaultExecutionContextComponents extends ExecutionContextComponents {
  self: AkkaComponents =>

  override implicit def ec: ExecutionContext = actorSystem.dispatcher
}


