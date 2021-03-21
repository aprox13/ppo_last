package ru.ifkbhit.ppo.backend

import akka.actor.ActorSystem
import akka.stream.Materializer
import ru.ifkbhit.ppo.common.model.config.ActorSystemConfig
import ru.ifkbhit.ppo.common.provider.ActorSystemProvider

trait AkkaComponents {

  val actorSystem: ActorSystem
  val materializer: Materializer
}

trait DefaultAkkaComponents extends AkkaComponents {

  def actorSystemConfig: ActorSystemConfig

  override lazy val actorSystem: ActorSystem = ActorSystemProvider.provide(actorSystemConfig)
  override lazy val materializer: Materializer = Materializer(actorSystem)

}
