package ru.ifkbhit.ppo.akka.actor

import akka.actor.ActorSystem
import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import ru.ifkbhit.ppo.akka.config.ApiActorConfig
import ru.ifkbhit.ppo.akka.manager.ApiManager.ApiResponse
import ru.ifkbhit.ppo.akka.manager.impl.{ApiManagerImpl, SearchManagerImpl}
import ru.ifkbhit.ppo.akka.model.SearchResponse
import ru.ifkbhit.ppo.akka.util.StubServerSpec
import ru.ifkbhit.ppo.common.model.config.ActorSystemConfig
import ru.ifkbhit.ppo.common.model.response.{Response, ResponseMatcher}
import ru.ifkbhit.ppo.common.provider.ActorSystemProvider

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ApiManagerSpec extends StubServerSpec with Matchers with MockFactory with ResponseMatcher {

  import ru.ifkbhit.ppo.akka.util.SearchStubServerSupport._

  private val engine1 = TestEngine("test1", 12344)
  private val engine2 = TestEngine("test2", 12346)
  private val engine3 = TestEngine("test3", 12348)
  private implicit var ac: ActorSystem = _


  registerStubs(
    engine1,
    engine2,
    engine3
  )

  override def beforeAll(): Unit = {
    super.beforeAll()

    ac = ActorSystemProvider.provide(ActorSystemConfig("test-system", 8))
  }

  override def afterAll(): Unit = {
    super.afterAll()

    Await.result(ac.terminate(), 1.second)
  }


  def makeCall(testEngines: TestEngine*): ApiResponse = {

    val searchManager = new SearchManagerImpl(enginesConfig(testEngines: _*))
    val manager = new ApiManagerImpl(searchManager, ApiActorConfig(100.millis))


    Await.result(manager.searchRequest("some")(ac.dispatcher), 5.second)
  }

  private def foundAnswer(engine: TestEngine): ApiResponse = {
    val r = SearchResponse(engine.name + " responded!")
    server(engine).shouldResponse(r)
    Map(engine.name -> Response.success(r))
  }

  private def timeoutAnswer(engine: TestEngine): ApiResponse = {
    server(engine).shouldTimeout

    Map(engine.name -> ApiActor.SearcherTimeoutResponse)
  }

  "ApiManager" should {
    "correctly timeout work with single searcher" when {

      "it timeout" in {

        val expected = timeoutAnswer(engine1)

        makeCall(engine1) shouldBe expected
      }

      "it respond" in {
        val expected = foundAnswer(engine1)

        makeCall(engine1) shouldBe expected
      }

    }

    "correctly work with multiple searchers" when {

      "all respond" in {
        val expected =
          foundAnswer(engine1) ++
            foundAnswer(engine2) ++
            foundAnswer(engine3)

        makeCall(engine1, engine2, engine3) shouldBe expected
      }

      "1 timeout" in {
        val expected =
          foundAnswer(engine1) ++
            foundAnswer(engine3) ++
            timeoutAnswer(engine2)

        makeCall(engine1, engine2, engine3) shouldBe expected
      }

      "2 timeout" in {
        val expected =
          timeoutAnswer(engine1) ++
            foundAnswer(engine3) ++
            timeoutAnswer(engine2)

        makeCall(engine1, engine2, engine3) shouldBe expected
      }

      "all timeout" in {
        val expected =
          timeoutAnswer(engine1) ++
            timeoutAnswer(engine3) ++
            timeoutAnswer(engine2)

        makeCall(engine1, engine2, engine3) shouldBe expected
      }

    }
  }
}

object FutureOps {

  implicit class FutureOps(val f: Future[ApiResponse]) extends AnyVal {

    def futureResult(duration: FiniteDuration): ApiResponse =
      Await.result(f, duration)
  }

}
