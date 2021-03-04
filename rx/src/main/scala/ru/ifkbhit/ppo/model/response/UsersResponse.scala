package ru.ifkbhit.ppo.model.response

import ru.ifkbhit.ppo.model.User
import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

case class UsersResponse(
  users: Seq[User]
)

object UsersResponse {
  implicit val JsonFormat: JsonFormat[UsersResponse] = jsonFormat1(UsersResponse(_))
}