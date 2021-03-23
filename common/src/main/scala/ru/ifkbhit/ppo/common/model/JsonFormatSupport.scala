package ru.ifkbhit.ppo.common.model

import spray.json._

abstract class JsonFormatSupport[T](format: RootJsonFormat[T]) {

  implicit val Format: RootJsonFormat[T] = format
}
