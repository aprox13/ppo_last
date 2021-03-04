package ru.ifkbhit.ppo.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling._

import scala.concurrent.{ExecutionContext, Future}

trait HttpClient {
  def doGet[A](path: String)(implicit ec: ExecutionContext, um: Unmarshaller[HttpResponse, A]): Future[A]
}


private class HttpClientImpl(implicit actorSystem: ActorSystem) extends HttpClient {
  override def doGet[A](path: String)(implicit ec: ExecutionContext, um: Unmarshaller[HttpResponse, A]): Future[A] = {
    Http().singleRequest(Get(path)).flatMap(Unmarshal(_).to[A])
  }
}
