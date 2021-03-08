package ru.ifkbhit.ppo.model.manager

import spray.json.DefaultJsonProtocol._
import spray.json._

case class UserPayload(name: String)

object UserPayload {
  implicit val format: RootJsonFormat[UserPayload] = jsonFormat1(UserPayload.apply)
}