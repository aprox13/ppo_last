package ru.ifkbhit.ppo.exception

class NotEnoughException(name: String) extends RuntimeException(s"There is not enough $name")

case object NotEnoughMoney extends NotEnoughException("money")

case object NotEnoughStocks extends NotEnoughException("stocks")

case object NotEnoughStocksAtUser extends NotEnoughException("stocks at user")

