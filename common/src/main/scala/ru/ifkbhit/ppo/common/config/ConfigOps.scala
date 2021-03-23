package ru.ifkbhit.ppo.common.config

import java.util.concurrent.TimeUnit

import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.util.Try

object ConfigOps {

  implicit class ConfigInterpStringOps(val context: StringContext)(implicit config: Config) {
    def cfg(args: Any*): Config = {
      val name = context.raw(args: _*)
      config.getConfig(name)
    }

    def str(args: Any*): String = {
      val name = context.raw(args: _*)
      StringProp.get(name)
    }

    def opts(args: Any*): Option[String] = {
      val name = context.raw(args: _*)
      StringProp.opt(name)
    }

    def opti(args: Any*): Option[Int] = {
      val name = context.raw(args: _*)
      IntProp.opt(name)
    }

    def int(args: Any*): Int = {
      val name = context.raw(args: _*)
      IntProp.get(name)
    }

    def optdur(args: Any*): Option[Duration] = {
      val name = context.raw(args: _*)
      DurationProp.opt(name)
    }

    def findur(args: Any*): FiniteDuration = {
      val name = context.raw(args: _*)
      DurationProp.get(name).asInstanceOf[FiniteDuration]
    }

    def bool(args: Any*): Boolean = {
      val name = context.raw(args: _*)
      BoolProp.get(name)
    }
  }


  private class Prop[A, R](
    getter: (Config, String) => A,
    filter: A => Boolean,
    mapper: A => R
  ) {
    def opt(name: String)(implicit config: Config): Option[R] = {
      Try(getter(config, name))
        .filter(filter)
        .map(mapper)
        .toOption
    }

    def get(name: String)(implicit config: Config): R = {
      opt(name).getOrElse(throw new RuntimeException(s"There is no $name in config"))
    }
  }

  private class SimpleProp[A](
    getter: (Config, String) => A,
    filter: A => Boolean = (_: A) => true
  ) extends Prop[A, A](getter, filter, identity)

  private object IntProp extends SimpleProp[Int](_.getInt(_))

  private object BoolProp extends SimpleProp[Boolean](_.getBoolean(_))

  private object StringProp
    extends SimpleProp[String](
      getter = _.getString(_),
      filter = s => Option(s).map(_.trim).exists(_.nonEmpty)
    )

  private object DurationProp extends Prop[Long, Duration](
    getter = _.getDuration(_, TimeUnit.MILLISECONDS),
    filter = _ => true,
    mapper = _.millis
  )

}
