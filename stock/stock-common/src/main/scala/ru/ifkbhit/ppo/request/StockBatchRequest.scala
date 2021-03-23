package ru.ifkbhit.ppo.request

import spray.json.DefaultJsonProtocol._
import spray.json._


case class StockBatchRequest[T](batch: Seq[T])

object StockBatchRequest {

  implicit def format[T: JsonFormat]: RootJsonFormat[StockBatchRequest[T]] =
    jsonFormat1(new StockBatchRequest[T](_))

}