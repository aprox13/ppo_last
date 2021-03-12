package ru.ifkbhit.ppo.gate

import java.sql.Connection

import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import ru.ifkbhit.ppo.actions.{DefaultManagerActions, ManagerActions}
import ru.ifkbhit.ppo.common.model.response.ResponseMatcher
import ru.ifkbhit.ppo.manager.ManagersManager
import ru.ifkbhit.ppo.manager.impl.{GateManagerImpl, ManagersManagerImpl}
import ru.ifkbhit.ppo.model.manager.{UserPayload, UserResult}
import ru.ifkbhit.ppo.utils.BaseManagerSpec

import scala.concurrent.duration._

class GateManagerSpec extends BaseManagerSpec with ResponseMatcher {

  private val managerActions: ManagerActions = new DefaultManagerActions(eventActions)
  private val managerManager: ManagersManager = new ManagersManagerImpl(stub[Connection], eventActions, managerActions)

  private def gateManager = new GateManagerImpl(connection, eventActions, managerActions)

  "GateManager" should {

    "pass existing user" when {

      "user enter" in {
        timer.useRealNow()

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue
          .as[UserResult]

        managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        timer.tick(1.minute)

        withSuccessTransaction {
          gateManager.enter(user.id).futureValue shouldBe successfulResponse
        }
      }

      "user exit" in {
        timer.useRealNow()

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue
          .as[UserResult]

        managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        timer.tick(1.minute)

        withSuccessTransaction {
          gateManager.enter(user.id).futureValue shouldBe successfulResponse
        }

        withSuccessTransaction {
          gateManager.exit(user.id).futureValue shouldBe successfulResponse
        }
      }

      "user enter after exit" in {
        timer.useRealNow()

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue
          .as[UserResult]

        managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        timer.tick(1.minute)

        withSuccessTransaction {
          gateManager.enter(user.id).futureValue shouldBe successfulResponse
        }

        withSuccessTransaction {
          gateManager.exit(user.id).futureValue shouldBe successfulResponse
        }

        withSuccessTransaction {
          gateManager.enter(user.id).futureValue shouldBe successfulResponse
        }
      }
    }

    "return error" when {
      "user not exists when enter" in {

        val response = withFailedTransaction {
          gateManager.enter(1111).futureValue
        }

        response shouldBe failedResponse("User not found: id=1111")
      }

      "user has no pass when enter" in {
        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue
          .as[UserResult]

        val response = withFailedTransaction {
          gateManager.enter(user.id).futureValue
        }

        response shouldBe failedResponse("No pass found for user")
      }

      "user pass already expire when enter" in {
        timer.setNow(DateTime.now().minusDays(10))

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue
          .as[UserResult]

        managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        timer.setNow(DateTime.now())


        val response =
          withFailedTransaction {
            gateManager.enter(user.id).futureValue
          }

        response shouldBe failedResponse("No pass found for user")
      }

      "user exit but no enter" in {
        timer.useRealNow()

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue
          .as[UserResult]

        managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        timer.tick(1.minute)

        withFailedTransaction {
          gateManager.exit(user.id).futureValue shouldBe failedResponse("User not enter yet")
        }
      }

      "user already enter" in {
        timer.useRealNow()

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue
          .as[UserResult]

        managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        timer.tick(1.minute)

        withSuccessTransaction {
          gateManager.enter(user.id).futureValue shouldBe successfulResponse
        }

        timer.tick(1.milli)

        withFailedTransaction {
          gateManager.enter(user.id).futureValue shouldBe failedResponse("User already enter")
        }
      }

      "user already exit" in {
        timer.useRealNow()

        val user = managerManager.addUser(UserPayload("user_name_1"))
          .futureValue
          .as[UserResult]

        managerManager.renewPass(user.id, 5).futureValue shouldBe successfulResponse
        timer.tick(1.minute)

        withSuccessTransaction {
          gateManager.enter(user.id).futureValue shouldBe successfulResponse
        }

        timer.tick(1.milli)

        withSuccessTransaction {
          gateManager.exit(user.id).futureValue shouldBe successfulResponse
        }

        withFailedTransaction {
          gateManager.exit(user.id).futureValue shouldBe failedResponse
        }
      }
    }

  }
}
