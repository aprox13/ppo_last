package ru.ifkbhit.ppo

import org.scalacheck.Gen
import ru.ifkbhit.ppo.model.Money
import ru.ifkbhit.ppo.request.StockItemCreateRequest

trait RequestGenerators {

  val CountGen: Gen[Long] = Gen.chooseNum(1, 10000)
  val MoneyGen: Gen[Money] = CountGen.map(_ * 100).map(Money(_))
  val NameGen: Gen[String] = Gen.alphaLowerStr


  val StockCreateRequestGen: Gen[StockItemCreateRequest] =
    for {
      name <- NameGen
      price <- MoneyGen
      count <- CountGen
    } yield StockItemCreateRequest(name, price, count)

}
