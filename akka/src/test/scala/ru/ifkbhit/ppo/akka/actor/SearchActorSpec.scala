package ru.ifkbhit.ppo.akka.actor

import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import ru.ifkbhit.ppo.akka.util.{StubServerSpec, TestSearcherClientProvider}

class SearchActorSpec extends StubServerSpec with Matchers with MockFactory {

  TestSearcherClientProvider.EnginesCfg
    .engines.values
    .map(_.endpoint.port)
    .foreach(registerStub)


  "sss" should {
    "correctly work" in  {

    }
  }

}
