package ru.ifkbhit.ppo

import rx.lang.scala.JavaConversions._
import rx.lang.scala.Observable

import scala.reflect.ClassTag

object RxOps {

  implicit class RichScalaObservable[T](val scala: Observable[T]) extends AnyVal {
    def asJava(implicit ct: ClassTag[T]): rx.Observable[T] =
      toJavaObservable(scala).cast(ct.runtimeClass.asInstanceOf[Class[T]])
  }

  implicit class RichJavaObservable[T](val java: rx.Observable[T]) extends AnyVal {
    def asScala: Observable[T] = toScalaObservable(java)
  }

  def fromCallable[T](body: => T): Observable[T] =
    Observable.defer {
      Observable.just(body)
    }

  def fail[T](throwable: Throwable): Observable[T] =
    fromCallable {
      throw throwable
    }

}
