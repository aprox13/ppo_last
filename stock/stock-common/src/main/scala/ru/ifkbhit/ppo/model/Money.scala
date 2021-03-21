package ru.ifkbhit.ppo.model

import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.math.Ordering.Implicits._
import scala.util.matching.Regex

case class Money(pennies: Long) {
  require(pennies >= 0, "Expected non negative pennies")

  def +(another: Money): Money =
    Money(pennies + another.pennies)

  def -(another: Money): Money = {
    require(this >= another, "Not enough money")

    this.copy(pennies = pennies - another.pennies)
  }

  def *(count: Long): Money =
    this.copy(pennies = pennies * count)
}


case object Money {

  val NoMoney: Money = Money(0)
  implicit val ordering: Ordering[Money] = Ordering.by(_.pennies)

  val StandardFormat: RootJsonFormat[Money] = jsonFormat1(Money(_))

  implicit val prettyToJsonFormat: RootJsonFormat[Money] = new RootJsonFormat[Money] {

    private val R: Regex = "^(\\d+)\\.(\\d\\d) ₽$".r

    override def write(obj: Money): JsValue = {
      val x: Double = obj.pennies.toDouble / 100.0f
      JsString(f"$x%.2f ₽")
    }

    override def read(json: JsValue): Money = {
      json match {
        case JsString(R(rubles, pennies)) if rubles != null && pennies != null =>
          Money(rubles.toLong * 100 + pennies.toLong)
        case x =>
          StandardFormat.read(x)
      }
    }
  }

}
