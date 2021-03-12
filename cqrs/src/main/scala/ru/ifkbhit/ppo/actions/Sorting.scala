package ru.ifkbhit.ppo.actions

import enumeratum.EnumEntry.Lowercase
import enumeratum._

trait Sorting extends EnumEntry

object Sorting {

  case object Desc extends Sorting with Lowercase

  case object Asc extends Sorting with Lowercase

}