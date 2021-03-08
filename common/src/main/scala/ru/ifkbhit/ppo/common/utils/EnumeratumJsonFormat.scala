package ru.ifkbhit.ppo.common.utils

import enumeratum._
import spray.json.{JsString, JsValue, JsonFormat}

trait EnumeratumJsonFormat[T <: EnumEntry] {
  self: Enum[T] =>

  implicit val jsonFormat: JsonFormat[T] = new JsonFormat[T] {
    override def read(json: JsValue): T =
      json match {
        case JsString(value) if withNameOption(value).isDefined =>
          withName(value)
        case _ =>
          throw new RuntimeException(s"Expected json strong with values ${values.map(_.entryName).mkString("[", ",", "]")}")
      }

    override def write(obj: T): JsValue =
      new JsString(obj.entryName)
  }
}
