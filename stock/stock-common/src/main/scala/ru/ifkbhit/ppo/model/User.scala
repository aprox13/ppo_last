package ru.ifkbhit.ppo.model

case class User(
  id: Option[Long],
  balance: Money,
  name: String
)
