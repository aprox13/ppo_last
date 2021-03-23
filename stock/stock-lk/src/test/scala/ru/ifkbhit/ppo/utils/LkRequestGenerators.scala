package ru.ifkbhit.ppo.utils

import org.scalacheck.Gen
import ru.ifkbhit.ppo.RequestGenerators
import ru.ifkbhit.ppo.request.UserCreateRequest

trait LkRequestGenerators {
  self: RequestGenerators =>

  val UserCreateRequestGen: Gen[UserCreateRequest] = NameGen.map(UserCreateRequest(_))
}
