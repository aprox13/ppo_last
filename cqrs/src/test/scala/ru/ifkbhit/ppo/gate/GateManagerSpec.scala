package ru.ifkbhit.ppo.gate

import java.sql.Connection

import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import ru.ifkbhit.ppo.actions.{DefaultManagerActions, ManagerActions}
import ru.ifkbhit.ppo.manager.GateManager._
import ru.ifkbhit.ppo.manager.ManagersManager
import ru.ifkbhit.ppo.manager.impl.{GateManagerImpl, ManagersManagerImpl}
import ru.ifkbhit.ppo.model.exception.UserNotFound
import ru.ifkbhit.ppo.model.gate.{UserEnterCommand, UserExitCommand}
import ru.ifkbhit.ppo.model.manager.{RenewPassCommand, UserPayload}
import ru.ifkbhit.ppo.utils.BaseManagerSpec
import ru.ifkbhit.ppo.utils.BaseManagerSpec.FutureOps

import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.Success

class GateManagerSpec extends BaseManagerSpec {

  private val managerActions: ManagerActions = new DefaultManagerActions(eventActions)
  private val managerManager: ManagersManager = new ManagersManagerImpl(stub[Connection], eventActions, managerActions)

  private def gateManager = new GateManagerImpl(connection, eventActions, managerActions)

  implicit def asUserEnter(userId: Long): UserEnterCommand = UserEnterCommand(userId)

  implicit def asUserExit(userId: Long): UserExitCommand = UserExitCommand(userId)

  "GateManager" should {

    "pass existing user" when {

      "user enter" in {
        timer.useRealNow()

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue

        managerManager.renewPass(RenewPassCommand(user.id, 5)) shouldBe successFuture
        timer.tick(1.minute)

        withSuccessTransaction {
          gateManager.enter(user.id).asTry shouldBe Success("User entered")
        }
      }

      "user exit" in {
        timer.useRealNow()

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue

        managerManager.renewPass(RenewPassCommand(user.id, 5)) shouldBe successFuture
        timer.tick(1.minute)

        withSuccessTransaction {
          gateManager.enter(user.id) shouldBe successFuture
        }

        withSuccessTransaction {
          gateManager.exit(user.id) shouldBe successFuture
        }
      }

      "user enter after exit" in {
        timer.useRealNow()

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue

        managerManager.renewPass(RenewPassCommand(user.id, 5)) shouldBe successFuture
        timer.tick(1.minute)

        withSuccessTransaction {
          gateManager.enter(user.id) shouldBe successFuture
        }

        withSuccessTransaction {
          gateManager.exit(user.id) shouldBe successFuture
        }

        withSuccessTransaction {
          gateManager.enter(user.id) shouldBe successFuture
        }
      }
    }

    "return error" when {
      "user not exists when enter" in {

        val response = withFailedTransaction {
          gateManager.enter(1111)
        }

        response shouldBe failureFuture(UserNotFound(1111))
      }

      "user has no pass when enter" in {
        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue

        val response = withFailedTransaction {
          gateManager.enter(user.id)
        }

        response shouldBe failureFuture(UserHasNoPass)
      }

      "user pass already expire when enter" in {
        timer.setNow(DateTime.now().minusDays(10))

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue

        managerManager.renewPass(RenewPassCommand(user.id, 5)) shouldBe successFuture
        timer.setNow(DateTime.now())


        val response =
          withFailedTransaction {
            gateManager.enter(user.id)
          }

        response shouldBe failureFuture(UserHasNoPass)
      }

      "user exit but no enter" in {
        timer.useRealNow()

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue

        managerManager.renewPass(RenewPassCommand(user.id, 5)) shouldBe successFuture
        timer.tick(1.minute)

        withFailedTransaction {
          gateManager.exit(user.id) shouldBe failureFuture(UserNotEnterYet)
        }
      }

      "user already enter" in {
        timer.useRealNow()

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue

        managerManager.renewPass(RenewPassCommand(user.id, 5)) shouldBe successFuture
        timer.tick(1.minute)

        withSuccessTransaction {
          gateManager.enter(user.id) shouldBe successFuture
        }

        timer.tick(1.milli)

        withFailedTransaction {
          gateManager.enter(user.id) shouldBe failureFuture(UserAlreadyEnter)
        }
      }

      "user already exit" in {
        timer.useRealNow()

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue

        managerManager.renewPass(RenewPassCommand(user.id, 5)) shouldBe successFuture
        timer.tick(1.minute)

        withSuccessTransaction {
          gateManager.enter(user.id) shouldBe successFuture
        }

        timer.tick(1.milli)

        withSuccessTransaction {
          gateManager.exit(user.id) shouldBe successFuture
        }

        withFailedTransaction {
          gateManager.exit(user.id) shouldBe failureFuture(UserAlreadyExit)
        }
      }
    }

  }
}
