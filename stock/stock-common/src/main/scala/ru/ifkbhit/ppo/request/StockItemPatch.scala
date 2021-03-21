package ru.ifkbhit.ppo.request

import ru.ifkbhit.ppo.model.Money
import spray.json.DefaultJsonProtocol._
import spray.json._

sealed trait StockItemPatch

object StockItemPatch {

  case class PricePatch(price: Money) extends StockItemPatch

  case class CountPatch(count: Long) extends StockItemPatch

  case class FullPatch(price: Money, count: Long) extends StockItemPatch


  private case class SupportPatch(price: Option[Money] = None, count: Option[Long] = None)

  private val supportFormat: RootJsonFormat[SupportPatch] = jsonFormat2(SupportPatch.apply)


  implicit val Format: RootJsonFormat[StockItemPatch] = new RootJsonFormat[StockItemPatch] {
    override def write(obj: StockItemPatch): JsValue =
      obj match {
        case PricePatch(price) =>
          supportFormat.write(SupportPatch(price = Some(price)))
        case CountPatch(count) =>
          supportFormat.write(SupportPatch(count = Some(count)))
        case FullPatch(price, count) =>
          supportFormat.write(SupportPatch(price = Some(price), count = Some(count)))
      }

    override def read(json: JsValue): StockItemPatch = {
      val support = supportFormat.read(json)

      support match {
        case SupportPatch(None, Some(count)) =>
          CountPatch(count)
        case SupportPatch(Some(price), None) =>
          PricePatch(price)
        case SupportPatch(Some(price), Some(count)) =>
          FullPatch(price, count)
        case _ =>
          throw new IllegalArgumentException("Expected one of 'price' or 'count'")
      }
    }
  }

}