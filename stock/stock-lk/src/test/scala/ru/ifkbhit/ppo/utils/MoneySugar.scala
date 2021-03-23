package ru.ifkbhit.ppo.utils

import ru.ifkbhit.ppo.model.Money

object MoneySugar {

  implicit class MoneySugar(val count: Int) extends AnyVal {

    def pennies: Money = Money(count)

    def rubles: Money = pennies * 100

    def thousand: Money = rubles * 1000
  }

}
