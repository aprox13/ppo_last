package ru.ifkbhit.ppo.service.currency

import ru.ifkbhit.ppo.model.{Currency, Price}
import spray.json._
import spray.json.DefaultJsonProtocol._

case class Conversion(
  base: Currency,
  rates: Map[Currency, Double]
) {

  def convert(price: Price): Option[Price] = {
    // to = from / rates[from]

    if (price.currency == base) {
      Some(price.copy())
    } else {
      rates.get(price.currency)
        .map { rate =>
          Price(
            currency = base,
            value = (price.value / rate).toLong
          )

        }
    }
  }
}


object Conversion {
  def empty(base: Currency): Conversion = Conversion(base, Map.empty)

  implicit val jsonFormat: JsonFormat[Conversion] = jsonFormat2(Conversion.apply)
}
