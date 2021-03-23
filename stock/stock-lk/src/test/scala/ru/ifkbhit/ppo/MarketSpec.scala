package ru.ifkbhit.ppo

import org.scalatest.concurrent.Futures
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.time.{Millis, Span}
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import ru.ifkbhit.ppo.common.model.response.Response.FailedResponseException
import ru.ifkbhit.ppo.common.utils.MapOps._
import ru.ifkbhit.ppo.components.{MarketHttpClientToContainerComponents, MarketInContainerComponents, TestDbComponents, UserTestBackend}
import ru.ifkbhit.ppo.db.PsqlTestContainerProvider
import ru.ifkbhit.ppo.future.FutureMatcher
import ru.ifkbhit.ppo.gen.GenOps.GenSugar
import ru.ifkbhit.ppo.model.StockItem
import ru.ifkbhit.ppo.model.response.user.{UserBalanceInfo, UserInfo, UserStockInfo}
import ru.ifkbhit.ppo.request.StockItemPatch.{FullPatch, PricePatch}
import ru.ifkbhit.ppo.request.{MarketRequest, StockBatchRequest, StockItemPatch, StockItemPatchRequest}
import ru.ifkbhit.ppo.utils.LkRequestGenerators
import ru.ifkbhit.ppo.utils.MoneySugar.MoneySugar

class MarketSpec
  extends WordSpec
    with Matchers
    with PsqlTestContainerProvider
    with BeforeAndAfter
    with Futures
    with RequestGenerators
    with LkRequestGenerators
    with FutureMatcher {

  object TestBackend
    extends UserTestBackend(testDbConfig)
      with MarketInContainerComponents
      with MarketHttpClientToContainerComponents
      with TestDbComponents

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(1000, Millis))


  var User: UserInfo = _
  var Stock1: StockItem = _
  var Stock2: StockItem = _

  def newUser: UserInfo =
    TestBackend.userManager.addUser(UserCreateRequestGen.next).futureValue(Timeout(Span(1000, Millis)))
      .ensuring(_.balance == UserBalanceInfo(0.rubles, 0.rubles, 0.rubles))
      .ensuring(_.stocks == Seq.empty)

  before {
    User = newUser

    val result = TestBackend.marketHttpClient.addStocks(
      StockBatchRequest(StockCreateRequestGen.next(2).toSeq)
    )

    Stock1 = result.head
    Stock2 = result.last
  }

  private def singlePatch(stock: StockItem, patches: StockItemPatch*) =
    TestBackend.marketHttpClient.patchStocks(
      StockBatchRequest(
        Seq(StockItemPatchRequest(stock.id.get, patches))
      )
    )

  private def marketRequest(user: UserInfo, stock: StockItem, count: Long) =
    MarketRequest(
      userId = user.id,
      stockId = stock.id.get,
      count = count
    )

  "Market" should {

    "correctly start in container" in {
      TestBackend.marketHttpClient.ping() shouldBe "pong"
    }

    "correctly return stock" in {
      val stock = StockCreateRequestGen.next

      val result = TestBackend.marketHttpClient.addStocks(
        StockBatchRequest(Seq(stock))
      ).applySideEffect(_ should have size 1).head

      val expected = StockItem.createForInsert(stock).copy(id = result.id)

      result shouldBe expected

      TestBackend.marketHttpClient.getStock(expected.id.get) shouldBe expected
    }

    "return error for non existing stock" in {
      the[FailedResponseException] thrownBy TestBackend.marketHttpClient.getStock(111) should have message "Item #111 not found!"
    }

    "return error when non existing user want to buy or sell" in {
      the[FailedResponseException] thrownBy TestBackend.marketHttpClient.buy(
        MarketRequest(1111, Stock1.id.get, count = 1)
      ) should have message "User with id 1111 not found"

      the[FailedResponseException] thrownBy TestBackend.marketHttpClient.sell(
        MarketRequest(1111, Stock1.id.get, count = 1)
      ) should have message "User with id 1111 not found"
    }

    "return error when existing user want to buy or sell non existing stock" in {
      the[FailedResponseException] thrownBy TestBackend.marketHttpClient.buy(
        MarketRequest(User.id, 123344, count = 1)
      ) should have message "Item #123344 not found!"

      the[FailedResponseException] thrownBy TestBackend.marketHttpClient.sell(
        MarketRequest(User.id, 123344, count = 1)
      ) should have message "Item #123344 not found!"
    }

    "correctly patch stocks" in {
      import TestBackend.marketHttpClient

      marketHttpClient.getStock(Stock1.id.get) shouldBe Stock1
      marketHttpClient.getStock(Stock2.id.get) shouldBe Stock2

      val result = marketHttpClient.patchStocks(
        StockBatchRequest(
          Seq(
            StockItemPatchRequest(
              Stock1.id.get,
              Seq(StockItemPatch.FullPatch(price = 100.rubles, count = 10))
            ),

            StockItemPatchRequest(
              Stock2.id.get,
              Seq(StockItemPatch.PricePatch(price = 12.rubles))
            )
          )
        )
      )

      val expected = Seq(Stock1.copy(price = 100.rubles, count = 10), Stock2.copy(price = 12.rubles))

      result should contain theSameElementsInOrderAs expected

      val resultByGet = Seq(Stock1, Stock2).flatMap(_.id)
        .map(marketHttpClient.getStock)

      resultByGet should contain theSameElementsInOrderAs expected
    }

    "return error if user has not enough money" in {
      import TestBackend.marketHttpClient
      val balance = User.balance.balance

      singlePatch(Stock1, PricePatch(price = balance + 10.rubles))

      the[FailedResponseException] thrownBy marketHttpClient.buy(
        MarketRequest(User.id, Stock1.id.get, 1)
      ) should have message "There is not enough money"
    }

    "return error if there is not enough stocks" in {
      import TestBackend.marketHttpClient

      TestBackend.userManager.addMoney(User.id, 200.rubles).futureValue

      singlePatch(Stock1, FullPatch(price = 90.rubles, count = 1))

      the[FailedResponseException] thrownBy marketHttpClient.buy(
        MarketRequest(User.id, Stock1.id.get, 2)
      ) should have message "There is not enough stocks"
    }

    "correctly buy stocks" in {
      import TestBackend.marketHttpClient

      TestBackend.userManager.addMoney(User.id, 200.rubles).futureValue

      User = User.copy(
        balance = User.balance.copy(
          balance = 200.rubles,
          total = 200.rubles
        )
      )
      TestBackend.userManager.getUserInfo(User.id) shouldBe successFuture(User)

      singlePatch(Stock1, FullPatch(price = 10.rubles, count = 10))
      Stock1 = Stock1.copy(
        price = 10.rubles,
        count = 10
      )


      marketHttpClient.buy(marketRequest(User, Stock1, 5)) shouldBe()

      TestBackend.userManager.getUserInfo(User.id) shouldBe successFuture(
        User.copy(
          balance = User.balance.copy(
            balance = 200.rubles - (10.rubles * 5),
            atStocks = 10.rubles * 5
          ),
          stocks = Seq(
            UserStockInfo(
              name = Stock1.name,
              count = 5,
              price = 10.rubles
            )
          )
        )
      )

      marketHttpClient.getStock(Stock1.id.get) shouldBe Stock1.copy(
        count = 5
      )
    }

    "correctly sell stock" in {
      import TestBackend.marketHttpClient

      TestBackend.userManager.addMoney(User.id, 200.rubles).futureValue

      User = User.copy(
        balance = User.balance.copy(
          balance = 200.rubles,
          total = 200.rubles
        )
      )
      TestBackend.userManager.getUserInfo(User.id) shouldBe successFuture(User)

      singlePatch(Stock1, FullPatch(price = 10.rubles, count = 10))
      Stock1 = Stock1.copy(
        price = 10.rubles,
        count = 10
      )


      marketHttpClient.buy(marketRequest(User, Stock1, 5)) shouldBe()

      TestBackend.userManager.getUserInfo(User.id) shouldBe successFuture(
        User.copy(
          balance = User.balance.copy(
            balance = 200.rubles - (10.rubles * 5),
            atStocks = 10.rubles * 5
          ),
          stocks = Seq(
            UserStockInfo(
              name = Stock1.name,
              count = 5,
              price = 10.rubles
            )
          )
        )
      )

      marketHttpClient.getStock(Stock1.id.get) shouldBe Stock1.copy(
        count = 5
      )

      marketHttpClient.sell(marketRequest(User, Stock1, 5))

      TestBackend.userManager.getUserInfo(User.id) shouldBe successFuture(
        User.copy(
          stocks = Seq.empty,
          balance = UserBalanceInfo(
            total = 200.rubles,
            atStocks = 0.rubles,
            balance = 200.rubles
          )
        )
      )

    }

    "correctly sell stock with changed price" in {
      import TestBackend.marketHttpClient

      TestBackend.userManager.addMoney(User.id, 200.rubles).futureValue

      User = User.copy(
        balance = User.balance.copy(
          balance = 200.rubles,
          total = 200.rubles
        )
      )
      TestBackend.userManager.getUserInfo(User.id) shouldBe successFuture(User)

      singlePatch(Stock1, FullPatch(price = 10.rubles, count = 10))
      Stock1 = Stock1.copy(
        price = 10.rubles,
        count = 10
      )


      marketHttpClient.buy(marketRequest(User, Stock1, 5)) shouldBe()

      TestBackend.userManager.getUserInfo(User.id) shouldBe successFuture(
        User.copy(
          balance = User.balance.copy(
            balance = 200.rubles - (10.rubles * 5),
            atStocks = 10.rubles * 5
          ),
          stocks = Seq(
            UserStockInfo(
              name = Stock1.name,
              count = 5,
              price = 10.rubles
            )
          )
        )
      )

      marketHttpClient.getStock(Stock1.id.get) shouldBe Stock1.copy(
        count = 5
      )

      singlePatch(Stock1, PricePatch(12.rubles))

      marketHttpClient.sell(marketRequest(User, Stock1, 5))

      TestBackend.userManager.getUserInfo(User.id) shouldBe successFuture(
        User.copy(
          stocks = Seq.empty,
          balance = UserBalanceInfo(
            total = 200.rubles + (12.rubles - 10.rubles) * 5,
            atStocks = 0.rubles,
            balance = 200.rubles + (12.rubles - 10.rubles) * 5
          )
        )
      )

    }

    "return error if user can't sell stock" in {
      import TestBackend.marketHttpClient

      TestBackend.userManager.addMoney(User.id, 200.rubles).futureValue

      User = User.copy(
        balance = User.balance.copy(
          balance = 200.rubles,
          total = 200.rubles
        )
      )
      TestBackend.userManager.getUserInfo(User.id) shouldBe successFuture(User)

      singlePatch(Stock1, FullPatch(price = 10.rubles, count = 10))
      Stock1 = Stock1.copy(
        price = 10.rubles,
        count = 10
      )


      marketHttpClient.buy(marketRequest(User, Stock1, 5)) shouldBe()

      TestBackend.userManager.getUserInfo(User.id) shouldBe successFuture(
        User.copy(
          balance = User.balance.copy(
            balance = 200.rubles - (10.rubles * 5),
            atStocks = 10.rubles * 5
          ),
          stocks = Seq(
            UserStockInfo(
              name = Stock1.name,
              count = 5,
              price = 10.rubles
            )
          )
        )
      )

      marketHttpClient.getStock(Stock1.id.get) shouldBe Stock1.copy(
        count = 5
      )

      singlePatch(Stock1, PricePatch(12.rubles))

      val userBeforeSell = TestBackend.userManager.getUserInfo(User.id).futureValue
      the[FailedResponseException] thrownBy marketHttpClient.sell(marketRequest(User, Stock1, 15)) should have message "There is not enough stocks at user"

      TestBackend.userManager.getUserInfo(User.id) shouldBe successFuture(userBeforeSell)
    }

  }
}
