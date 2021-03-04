package ru.ifkbhit.ppo.model

import org.bson.Document
import ru.ifkbhit.ppo.model.format.DocFormat
import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

case class Price(
  value: Long,
  currency: Currency
) {

  def prettyPrint: String = {
    val normalized = value.toDouble / currency.coefficientToOne

    f"$normalized%.2f ${currency.symbol}%s"
  }
}

object Price {

  implicit val jsonFormat: JsonFormat[Price] = jsonFormat2(Price.apply)
  implicit val docFormat: DocFormat[Price] = new DocFormat[Price] {
    override def read(document: Document): Price =
      Price(
        value = document.getLong("price_value"),
        currency = Currency.resolve(document.getString("price_currencyCode"))
      )

    override def write(element: Price): Document =
      new Document()
        .append("price_value", element.value)
        .append("price_currencyCode", element.currency.entryName)
  }

}
