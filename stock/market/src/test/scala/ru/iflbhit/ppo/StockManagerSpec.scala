package ru.iflbhit.ppo

import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.{Matchers, WordSpec}
import ru.ifkbhit.ppo.backend.{DefaultDatabaseComponents, DefaultStockActionsComponents, DefaultStockManagerComponents}
import ru.ifkbhit.ppo.common.utils.MapOps._
import ru.ifkbhit.ppo.database.provider.DbConfig
import ru.ifkbhit.ppo.db.PsqlTestContainerProvider
import ru.ifkbhit.ppo.exception.ItemNotFound
import ru.ifkbhit.ppo.future.FutureMatcher
import ru.ifkbhit.ppo.gen.GenOps._
import ru.ifkbhit.ppo.model.StockItem
import ru.ifkbhit.ppo.request.StockItemPatch.{CountPatch, PricePatch}
import ru.ifkbhit.ppo.request.{StockItemPatch, StockItemPatchRequest}
import ru.ifkbhit.ppo.{RequestGenerators, TestExecutionContextComponents}

class StockManagerSpec
  extends WordSpec
    with Matchers
    with PsqlTestContainerProvider
    with FutureMatcher
    with RequestGenerators {

  object TestBackend extends TestExecutionContextComponents
    with DefaultDatabaseComponents
    with DefaultStockActionsComponents
    with DefaultStockManagerComponents {
    override def dbConfig: DbConfig = testDbConfig
  }

  private lazy val manager = TestBackend.stockManager

  "StockManager" should {

    "return error on non existing stock" in {
      manager.getStock(1111) shouldBe failureFuture(ItemNotFound(1111))
    }

    "correctly create" when {
      def batchSpec(count: Int): Unit = {
        val batch = StockCreateRequestGen.next(count)
        manager.addStocks(batch.toSeq)
          .futureValue
          .applySideEffect(_ should have size count)
          .zip(batch)
          .foreach {
            case (result, request) =>
              result.id should not be None
              val id = result.id

              val expected = StockItem.createForInsert(request).copy(id = id)

              manager.getStock(id.get) shouldBe successFuture(expected)
          }
      }

      "comes single stock" in {
        batchSpec(1)
      }

      "comes batch" in {
        batchSpec(2)
        batchSpec(3)
      }
    }

    "correctly patch" when {

      def insertOne(): StockItem =
        manager.addStocks(Seq(StockCreateRequestGen.next)).futureValue.head
          .ensuring(_.id.isDefined)


      "comes price patch request" in {
        val item = insertOne()
        val patch = StockItemPatch.PricePatch(MoneyGen.next)

        manager.patchStocks(Seq(StockItemPatchRequest(item.id.get, Seq(patch)))) shouldBe successFuture[Seq[StockItem]]

        manager.getStock(item.id.get) shouldBe successFuture(item.copy(price = patch.price))
      }

      "comes count patch request" in {
        val item = insertOne()
        val patch = StockItemPatch.CountPatch(CountGen.next)

        manager.patchStocks(Seq(StockItemPatchRequest(item.id.get, Seq(patch)))) shouldBe successFuture[Seq[StockItem]]

        manager.getStock(item.id.get) shouldBe successFuture(item.copy(count = patch.count))
      }

      "comes price and count patches" in {
        val item = insertOne()
        val patches = Seq(
          StockItemPatch.PricePatch(MoneyGen.next),
          StockItemPatch.CountPatch(CountGen.next)
        )
        manager.patchStocks(Seq(StockItemPatchRequest(item.id.get, patches))) shouldBe successFuture[Seq[StockItem]]

        manager.getStock(item.id.get) shouldBe successFuture(
          item.copy(
            price = patches.head.asInstanceOf[PricePatch].price,
            count = patches.last.asInstanceOf[CountPatch].count
          )
        )
      }

      "comes full patch" in {
        val item = insertOne()
        val patches = Seq(
          StockItemPatch.FullPatch(
            price = MoneyGen.next, count = CountGen.next
          )
        )
        manager.patchStocks(Seq(StockItemPatchRequest(item.id.get, patches))) shouldBe successFuture[Seq[StockItem]]

        manager.getStock(item.id.get) shouldBe successFuture(
          item.copy(
            price = patches.head.price,
            count = patches.head.count
          )
        )
      }
    }

  }


}
