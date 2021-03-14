package ru.ifkbhit.ppo.model.exception

case class UserNotFound(id: Long) extends RuntimeException(s"User not found: id=$id")
