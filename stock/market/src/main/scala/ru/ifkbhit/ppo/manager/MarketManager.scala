package ru.ifkbhit.ppo.manager

import ru.ifkbhit.ppo.request.MarketRequest

import scala.concurrent.Future

trait MarketManager {

  def sell(marketRequest: MarketRequest): Future[Unit]

  def buy(marketRequest: MarketRequest): Future[Unit]
}
