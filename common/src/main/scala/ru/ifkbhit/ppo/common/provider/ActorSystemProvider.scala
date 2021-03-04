package ru.ifkbhit.ppo.common.provider

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import ru.ifkbhit.ppo.common.model.config.ActorSystemConfig

import scala.concurrent.ExecutionContext

object ActorSystemProvider {

  def provide(cfg: ActorSystemConfig): ActorSystem = {
    val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(cfg.pools))
    ActorSystem(
      name = cfg.name,
      config = None,
      classLoader = None,
      defaultExecutionContext = Some(ec)
    )
  }

}
