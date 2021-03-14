package ru.ifkbhit.ppo.util

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json.{JsString, JsValue, RootJsonFormat, deserializationError}

object DateTimeUtils {

  implicit object DateTimeFormat extends RootJsonFormat[DateTime] {

    private val formatter = ISODateTimeFormat.basicDateTimeNoMillis

    def write(obj: DateTime): JsValue = {
      JsString(formatter.print(obj))
    }

    def read(json: JsValue): DateTime = json match {
      case JsString(s) => try {
        formatter.parseDateTime(s)
      }
      catch {
        case _: Throwable => error(s)
      }
      case _ =>
        error(json.toString())
    }

    def error(v: Any): DateTime = {
      val example = formatter.print(0)
      deserializationError(f"'$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
    }
  }

  implicit class DateTimeOps(val dt: DateTime) extends AnyVal {

    def asPsqlTimestamp: String =
      org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").print(dt)
  }

  implicit object DateTimeOrdering extends Ordering[DateTime] {
    override def compare(x: DateTime, y: DateTime): Int = x.compareTo(y)
  }

}
