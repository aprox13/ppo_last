package ru.ifkbhit.ppo.handler

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ru.ifkbhit.ppo.common.handler.{BaseDirectives, JsonAnsweredHandler}
import ru.ifkbhit.ppo.manager.{MarketManager, StockManager}
import ru.ifkbhit.ppo.request.{MarketRequest, StockBatchRequest}
import spray.json.{JsonFormat, RootJsonFormat}

import scala.concurrent.{ExecutionContext, Future}

class MarketHandler(marketManager: MarketManager, stockManager: StockManager)(implicit val ec: ExecutionContext) extends JsonAnsweredHandler with BaseDirectives {

  private def stockBatchPostRoute[T: JsonFormat, R: JsonFormat](
    actionInPath: String
  )(producer: Seq[T] => Future[R]): Route = {
    implicit val format: RootJsonFormat[StockBatchRequest[T]] = StockBatchRequest.format[T]

    (post & path("market" / "stocks" / actionInPath) & entity(as[StockBatchRequest[T]])) { request =>
      completeResponse(producer(request.batch))
    }
  }

  private def marketActionRoute(actionName: String)(producer: MarketRequest => Future[Unit]): Route =
    (post & path("market" / actionName) & entity(as[MarketRequest])) { request =>
      completeResponse(producer(request))
    }

  private def addBatchRoute: Route = stockBatchPostRoute("add")(stockManager.addStocks)

  private def patchBatchRoute: Route = stockBatchPostRoute("patch")(stockManager.patchStocks)

  private def sellRoute: Route =
    marketActionRoute("sell")(marketManager.sell)

  private def buyRoute: Route =
    marketActionRoute("buy")(marketManager.buy)

  private def singleStockRoute: Route =
    (get & path("market" / "stock" / LongNumber)) { id =>
      completeResponse(stockManager.getStock(id))
    }

  override def route: Route =
    singleStockRoute ~ addBatchRoute ~ patchBatchRoute ~ sellRoute ~ buyRoute
}
