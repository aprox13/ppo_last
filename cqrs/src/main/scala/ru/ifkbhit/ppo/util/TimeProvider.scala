package ru.ifkbhit.ppo.util

import org.joda.time.DateTime

trait TimeProvider {

  def now(): DateTime
}

object SimpleTimeProvider extends TimeProvider {
  override def now(): DateTime = DateTime.now().withMillisOfSecond(0)
}
