package ru.ifkbhit.ppo.model.stat

import enumeratum.EnumEntry.Lowercase
import enumeratum.{Enum, EnumEntry}
import ru.ifkbhit.ppo.common.utils.EnumeratumJsonFormat
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.collection.immutable
import scala.concurrent.duration._

case class FrequencyReport(
  value: Double,
  per: FrequencyReport.Frequency
)

object FrequencyReport {

  implicit val format: RootJsonFormat[FrequencyReport] = jsonFormat2(FrequencyReport.apply)

  sealed class Frequency(val periodMillis: Long) extends EnumEntry

  object Frequency extends Enum[Frequency] with EnumeratumJsonFormat[Frequency] {

    case object Day extends Frequency(1.day.toMillis) with Lowercase

    case object Week extends Frequency(7.days.toMillis) with Lowercase

    case object ThirtyDays extends Frequency(30.days.toMillis) {
      override def entryName: String = "30-days"
    }

    override def values: immutable.IndexedSeq[Frequency] = findValues
  }

}
