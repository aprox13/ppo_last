package ru.ifkbhit.ppo.exception

case class ItemNotFound(id: Long) extends RuntimeException(s"Item #$id not found!")
