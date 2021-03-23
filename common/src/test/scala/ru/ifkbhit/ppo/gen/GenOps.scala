package ru.ifkbhit.ppo.gen

import org.scalacheck.Gen

object GenOps {

  implicit class GenSugar[T](val gen: Gen[T]) extends AnyVal {
    def iterator: Iterator[T] =
      Iterator.continually(gen.sample).flatten

    def next: T = iterator.next()

    def next(count: Int): Iterable[T] = iterator.take(count).toIterable
  }

}
