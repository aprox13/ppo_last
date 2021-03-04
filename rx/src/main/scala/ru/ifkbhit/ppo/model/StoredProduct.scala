package ru.ifkbhit.ppo.model

import org.bson.Document
import ru.ifkbhit.ppo.model.format.DocFormat
import spray.json.DefaultJsonProtocol._
import spray.json._

case class StoredProduct(
  id: Long,
  name: String,
  price: Price
)

object StoredProduct {
  implicit val jsonFormat: JsonFormat[StoredProduct] = jsonFormat3(StoredProduct.apply)

  implicit val docFormat: DocFormat[StoredProduct] = new DocFormat[StoredProduct] {
    override def read(document: Document): StoredProduct = {
      val price = Price.docFormat.read(document)

      StoredProduct(
        id = document.getLong("id"),
        name = document.getString("name"),
        price = price
      )
    }

    override def write(element: StoredProduct): Document =
      Price.docFormat
        .write(element.price)
        .append("id", element.id)
        .append("name", element.name)
  }

}
