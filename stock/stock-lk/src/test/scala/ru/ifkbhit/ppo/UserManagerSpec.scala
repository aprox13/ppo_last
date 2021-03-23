package ru.ifkbhit.ppo

import org.scalatest.concurrent.Futures
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.time.{Millis, Span}
import org.scalatest.{Matchers, WordSpec}
import ru.ifkbhit.ppo.components.UserTestBackend
import ru.ifkbhit.ppo.db.PsqlTestContainerProvider
import ru.ifkbhit.ppo.exception.UserNotFound
import ru.ifkbhit.ppo.future.FutureMatcher
import ru.ifkbhit.ppo.gen.GenOps._
import ru.ifkbhit.ppo.manager.UserManager
import ru.ifkbhit.ppo.model.Money
import ru.ifkbhit.ppo.utils.LkRequestGenerators
import ru.ifkbhit.ppo.utils.MoneySugar._

class UserManagerSpec extends WordSpec
  with Matchers
  with PsqlTestContainerProvider
  with FutureMatcher
  with RequestGenerators
  with LkRequestGenerators
  with Futures {

  object TestBackend extends UserTestBackend(testDbConfig)

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(1000, Millis))

  val manager: UserManager = TestBackend.userManager

  "UserManager" should {
    "return error on non existing user" in {
      manager.getUserInfo(1111) shouldBe failureFuture(UserNotFound(1111))
    }

    "successfully add user" in {
      val request = UserCreateRequestGen.next
      val result = manager.addUser(request).futureValue

      result.name shouldBe request.name
      result.balance.total shouldBe Money.NoMoney
      result.stocks shouldBe empty

      manager.getUserInfo(result.id) shouldBe successFuture(result)
    }

    "add money to balance" in {
      val request = UserCreateRequestGen.next
      val user = manager.addUser(request).futureValue

      user.balance.total shouldBe 0.pennies

      val toAdd = 100.rubles + 32.pennies
      manager.addMoney(user.id, toAdd) shouldBe successFuture

      manager.getUserInfo(user.id) shouldBe successFuture(
        user.copy(
          balance = user.balance.copy(
            balance = toAdd,
            total = toAdd
          )
        )
      )
    }
  }
}
