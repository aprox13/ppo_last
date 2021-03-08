package ru.ifkbhit.ppo.akka.model

import spray.json.DefaultJsonProtocol._
import spray.json._


case class SearchResponse(text: String)


object SearchResponse {
  val Failed: SearchResponse = SearchResponse("err")

  implicit val format: RootJsonFormat[SearchResponse] = jsonFormat1(SearchResponse.apply)
}