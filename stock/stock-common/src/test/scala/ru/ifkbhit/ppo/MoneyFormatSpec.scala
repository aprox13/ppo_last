package ru.ifkbhit.ppo

import org.scalatest.{Matchers, WordSpec}
import ru.ifkbhit.ppo.model.Money
import spray.json._

class MoneyFormatSpec extends WordSpec with Matchers {

  private val StandardExamples: String =
    """{
      | "pennies": 100012
      |}""".stripMargin

  private val PrettyExamples: String = "\"123.12 ₽\""


  "Money json format" should {
    "correctly parse standard" in {
      StandardExamples.parseJson.convertTo[Money] shouldBe Money(100012)
    }

    "correctly parse pretty" in {
      PrettyExamples.parseJson.convertTo[Money] shouldBe Money(12312)
    }
  }
}
