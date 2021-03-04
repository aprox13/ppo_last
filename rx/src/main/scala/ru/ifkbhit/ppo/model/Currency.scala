package ru.ifkbhit.ppo.model

import enumeratum.EnumEntry.Uppercase
import enumeratum._
import ru.ifkbhit.ppo.Exceptions
import spray.json.{JsString, JsValue, JsonFormat}

import scala.collection.immutable

/**
 * @param coefficientToOne - число, на которое надо поделить, для точности 1.
 *                         Например, для рублей это 100, так как в 1 рубле 100 копеек
 */
sealed abstract class Currency(val coefficientToOne: Int, val symbol: String) extends EnumEntry


object Currency extends Enum[Currency] {

  implicit val jsonFormat: JsonFormat[Currency] = new JsonFormat[Currency] {
    override def read(json: JsValue): Currency =
      json match {
        case JsString(value) =>
          resolve(value)
        case x =>
          throw Exceptions.BadRequest(s"Expected currency string, got $x")
      }

    override def write(obj: Currency): JsValue = JsString(obj.entryName)
  }


  override def values: immutable.IndexedSeq[Currency] = findValues

  def resolve(currencyCode: String): Currency =
    values.find(_.entryName == currencyCode)
      .ensuring(_.isDefined, s"Unknown currency $currencyCode")
      .get


  case object Rub extends Currency(100, "₽") with Uppercase
  case object Usd extends Currency(100, "$") with Uppercase
  case object Eur extends Currency(100, "€") with Uppercase
}
