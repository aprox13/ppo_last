package ru.ifkbhit.ppo.akka.model

case class SearchResponse(text: String)


object SearchResponse {
  val Failed: SearchResponse = SearchResponse("err")
}