package ru.ifkbhit.ppo.common.utils

object MapOps {

  implicit class MapOps[A](val x: A) extends AnyVal {
    def applyTransform[B](f: A => B): B = f(x)

    def applySideEffect(f: A => Any): A = {
      f(x); x
    }

    def applyTransformIf(test: Boolean)(f: A => A): A = if (test) f(x) else x

    def applyTransformIfPred(test: A => Boolean)(f: A => A): A = if (test(x)) f(x) else x

    def collect[B](pf: PartialFunction[A, B]): Option[B] = Some(x).collectFirst(pf)
  }

}
