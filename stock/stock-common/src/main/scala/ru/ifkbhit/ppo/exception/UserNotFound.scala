package ru.ifkbhit.ppo.exception

case class UserNotFound(id: Long) extends RuntimeException(s"User with id $id not found")
