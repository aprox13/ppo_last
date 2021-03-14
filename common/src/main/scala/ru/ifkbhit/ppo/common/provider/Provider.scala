package ru.ifkbhit.ppo.common.provider

trait Provider[T] {
  def get: T
}
