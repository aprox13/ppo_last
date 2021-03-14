package ru.ifkbhit.ppo.common.provider

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{ScheduledExecutorService, TimeUnit}

import ru.ifkbhit.ppo.common.utils.AtomicRefOps.AtomicRefOps

import scala.concurrent.duration.FiniteDuration

/**
 * Сохраняет результат get в памяти, обновляя его каждый refreshDelay. При запуске блокирующе подгружает данные
 */
trait CachedProvider[T] extends Provider[T] {

  private val cache: AtomicReference[T] = new AtomicReference[T]()

  protected def scheduler: ScheduledExecutorService

  protected def load: T = super.get

  protected def refreshDelay: FiniteDuration

  private def init(): Unit = {
    cache.set(load)

    scheduler.scheduleWithFixedDelay(
      () => cache.set(load),
      refreshDelay.toMillis,
      refreshDelay.toMillis,
      TimeUnit.MILLISECONDS
    )
  }

  abstract override def get: T = cache.opt.getOrElse(throw new RuntimeException("No data!"))

  init()
}
