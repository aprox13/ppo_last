package ru.ifkbhit.ppo.manager

import ru.ifkbhit.ppo.common.model.response.Response

import scala.concurrent.Future

trait GateManager {
  def enter(userId: Long): Future[Response]

  def exit(userId: Long): Future[Response]
}
