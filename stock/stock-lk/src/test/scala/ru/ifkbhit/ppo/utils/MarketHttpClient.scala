package ru.ifkbhit.ppo.utils

import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.model.config.HttpEndpoint
import ru.ifkbhit.ppo.common.model.response.Response
import ru.ifkbhit.ppo.model.StockItem
import ru.ifkbhit.ppo.request._
import scalaj.http._
import spray.json.DefaultJsonProtocol._
import spray.json.{JsonFormat, _}

import scala.util.Try

class MarketHttpClient(marketEndpoint: HttpEndpoint) {

  import MarketHttpClient._

  private def forPath(path: String) = {
    require(path.isEmpty || path.startsWith("/"))
    Http(s"${marketEndpoint.toUrl}$path")
  }


  def sell(request: MarketRequest): Unit =
    forPath("/market/sell")
      .postBody(request)
      .getJsonResponse[Unit]


  def buy(request: MarketRequest): Unit =
    forPath("/market/buy")
      .postBody(request)
      .getJsonResponse[Unit]

  def getStock(id: Long): StockItem =
    forPath(s"/market/stock/$id")
      .method("GET")
      .getJsonResponse[StockItem]

  def addStocks(request: StockBatchRequest[StockItemCreateRequest]): Seq[StockItem] =
    forPath(s"/market/stocks/add")
      .postBody[StockBatchRequest[StockItemCreateRequest]](request)
      .getJsonResponse[Seq[StockItem]]

  def patchStocks(request: StockBatchRequest[StockItemPatchRequest]): Seq[StockItem] =
    forPath(s"/market/stocks/patch")
      .postBody[StockBatchRequest[StockItemPatchRequest]](request)
      .getJsonResponse[Seq[StockItem]]

  def ping(): String =
    forPath("/ping")
      .getStringResponse

}

object MarketHttpClient extends Logging {

  private implicit class RequestOps(val r: HttpRequest) extends AnyVal {

    def getStringResponse: String = {
      val body = Try(r.connectFunc.asInstanceOf[StringBodyConnectFunc].data)
        .toOption
        .map(s => s"[$s]")
        .getOrElse("")
      log.info(s">> [${r.method}] ${r.url} $body")

      val res = r.asString.body
      log.info(s"<< $res")
      res
    }

    def getJsonResponse[T: JsonFormat]: T =
      new Response.ResponseReaderFormat[T].read(
        getStringResponse.parseJson
      )

    def postBody[T: JsonFormat](body: T): HttpRequest =
      r.postData(body.toJson.compactPrint)
        .header("Content-Type", "application/json")
  }

}
