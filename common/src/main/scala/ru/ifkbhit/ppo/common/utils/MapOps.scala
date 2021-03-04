package ru.ifkbhit.ppo.common.utils

object MapOps {

  implicit class MapOps[A](val x: A) extends AnyVal {
    def applyTransform(f: A => A): A = f(x)
    def applySideEffect(f: A => Any): A = { f(x); x }

    def applyTransformIf(test: Boolean)(f: A => A): A = if (test) f(x) else x
  }

}
