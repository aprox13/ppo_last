package ru.ifkbhit.ppo.request

import ru.ifkbhit.ppo.common.model.JsonFormatSupport
import spray.json.DefaultJsonProtocol._

case class UserCreateRequest(name: String)


object UserCreateRequest extends JsonFormatSupport[UserCreateRequest](jsonFormat1(new UserCreateRequest(_)))