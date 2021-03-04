package ru.ifkbhit.ppo.model

import org.bson.Document
import ru.ifkbhit.ppo.model.format.DocFormat
import spray.json.DefaultJsonProtocol._
import spray.json._


case class User(id: Long, currency: Currency)

object User {
  implicit val jsonFormat: JsonFormat[User] = jsonFormat2(User.apply)
  implicit val docFormat: DocFormat[User] = new DocFormat[User] {
    override def read(document: Document): User =
      User(
        id = document.getLong("id"),
        currency = Currency.resolve(document.getString("currency"))
      )

    override def write(element: User): Document =
      new Document()
        .append("id", element.id)
        .append("currency", element.currency.entryName)
  }
}