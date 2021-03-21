package ru.ifkbhit.ppo.handler

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ru.ifkbhit.ppo.common.handler.{BaseDirectives, JsonAnsweredHandler}
import ru.ifkbhit.ppo.manager.{MarketManager, StockManager}
import ru.ifkbhit.ppo.request.StockBatchRequestFormat.format
import ru.ifkbhit.ppo.request.{MarketRequest, StockBatchRequest, StockItemCreateRequest, StockItemPatchRequest}

import scala.concurrent.ExecutionContext

class MarketHandler(marketManager: MarketManager, stockManager: StockManager)(implicit val ec: ExecutionContext) extends JsonAnsweredHandler with BaseDirectives {

  override def route: Route =
    path("market") {
      (post & entity(as[MarketRequest])) { body =>
        path("sell") {
          completeResponse(marketManager.sell(body))
        } ~
          path("buy") {
            completeResponse(marketManager.buy(body))
          }
      } ~
        (get & path("stock" / LongNumber)) { id =>
          completeResponse(stockManager.getStock(id))
        } ~
        (post & path("stocks")) {
          (path("patch") & entity(as[StockBatchRequest[StockItemPatchRequest]])) { body =>
            completeResponse(stockManager.patchStocks(body.batch))
          } ~
            (path("add") & entity(as[StockBatchRequest[StockItemCreateRequest]])) { body =>
              completeResponse(stockManager.addStocks(body.batch))
            }
        }
    }
}
