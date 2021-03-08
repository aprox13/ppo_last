package ru.ifkbhit.ppo.model.event

import enumeratum._
import ru.ifkbhit.ppo.common.utils.EnumeratumJsonFormat

import scala.collection.immutable

sealed trait EventType extends EnumEntry {

}

object EventType extends Enum[EventType] with EnumeratumJsonFormat[EventType] {

  case object CreateUser extends EventType

  case object CreatePass extends EventType

  case object RenewPass extends EventType

  case object UserEntered extends EventType

  case object UserExit extends EventType

  override def values: immutable.IndexedSeq[EventType] = findValues

}


