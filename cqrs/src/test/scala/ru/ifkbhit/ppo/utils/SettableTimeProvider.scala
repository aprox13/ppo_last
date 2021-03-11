package ru.ifkbhit.ppo.utils

import java.util.concurrent.atomic.AtomicReference

import org.joda.time.DateTime
import ru.ifkbhit.ppo.common.utils.AtomicRefOps.AtomicRefOps
import ru.ifkbhit.ppo.util.TimeProvider

import scala.concurrent.duration.FiniteDuration

trait SettableTimeProvider extends TimeProvider {
  val time: AtomicReference[DateTime] = new AtomicReference[DateTime]()

  def setNow(dateTime: DateTime): Unit = time.set(dateTime)

  def tick(finiteDuration: FiniteDuration): Unit = {
    require(time.opt.isDefined, "Time not set")

    time.set(
      time.get().plus(finiteDuration.toMillis)
    )
  }

  def useRealNow(): Unit =
    setNow(DateTime.now())

  override def now(): DateTime = time.opt.getOrElse(throw new RuntimeException("Time wasn't set"))
}

object SettableTimeProvider extends SettableTimeProvider

