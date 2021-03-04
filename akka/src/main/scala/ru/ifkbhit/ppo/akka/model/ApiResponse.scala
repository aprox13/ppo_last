package ru.ifkbhit.ppo.akka.model

import ru.ifkbhit.ppo.common.model.response.Response

case class ApiResponse(result: Map[String, Response[SearchResponse]])
