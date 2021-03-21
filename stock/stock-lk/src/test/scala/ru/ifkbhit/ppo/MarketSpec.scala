package ru.ifkbhit.ppo

import org.scalatest.{Matchers, WordSpec}
import ru.ifkbhit.ppo.db.PsqlTestContainerProvider
import ru.ifkbhit.ppo.utils.BasicMarketContainer
import scalaj.http._

class MarketSpec extends WordSpec with Matchers with PsqlTestContainerProvider {

  private lazy val marketContainer: BasicMarketContainer = {
    val res = new BasicMarketContainer(8080, testDbConfig)

    sys.addShutdownHook(res.stop())
    res.start()

    res
  }

  identity(marketContainer)


  "Market" should {

    "correctly work" in {
      Thread.sleep(100000000)
      val res = Http(s"http://localhost:${marketContainer.getMappedApiPort}/stock/1111")
        .asString.body


      println(res)
    }
  }
}
