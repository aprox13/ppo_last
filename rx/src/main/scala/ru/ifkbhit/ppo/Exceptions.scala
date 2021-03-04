package ru.ifkbhit.ppo

object Exceptions {

  case class NotFound(what: String) extends RuntimeException(s"Error, $what not found!")
  case class BadRequest(msg: String) extends RuntimeException(msg)

  case class Duplicated[T](name: String, id: T) extends RuntimeException(s"$name with id `$id` already exists")
}
