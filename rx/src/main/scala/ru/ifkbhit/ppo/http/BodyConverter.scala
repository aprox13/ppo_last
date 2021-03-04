package ru.ifkbhit.ppo.http

import java.nio.charset.Charset

import io.netty.buffer.ByteBuf

trait BodyConverter[T, R] extends (T => R)


object BodyConverters {
  implicit object ByteToStringConverter extends BodyConverter[ByteBuf, String] {
    override def apply(v1: ByteBuf): String = v1.toString(Charset.forName("UTF-8"))
  }


  implicit class RichBodyConverter[T, R](val bodyConverter: BodyConverter[T, R]) extends AnyVal {
    def thenParse[M](next: BodyConverter[R, M]): BodyConverter[T, M] =
      new BodyConverter[T, M] {
        override def apply(v1: T): M = {
          val x = bodyConverter(v1)

          next(x)
        }
      }
  }
}
