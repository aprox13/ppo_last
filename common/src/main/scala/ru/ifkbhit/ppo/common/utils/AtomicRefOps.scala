package ru.ifkbhit.ppo.common.utils

import java.util.concurrent.atomic.AtomicReference

object AtomicRefOps {

  implicit class AtomicRefOps[A](val ref: AtomicReference[A]) extends AnyVal {

    def opt: Option[A] = Option(ref.get())
    def exists(f: A => Boolean): Boolean = opt.exists(f)
    def foreach(f: A => Any): Unit = opt.foreach(f)
  }

}
