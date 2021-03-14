package ru.ifkbhit.ppo.model.stat

import ru.ifkbhit.ppo.model.event.Interval

case class StatQuery(
  userId: Long,
  interval: Interval,
  frequency: FrequencyReport.Frequency
)
