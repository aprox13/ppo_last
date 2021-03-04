package ru.ifkbhit.ppo.http

import io.netty.handler.codec.http.HttpMethod
import io.reactivex.netty.protocol.http.server.HttpServerRequest
import ru.ifkbhit.ppo.RxOps
import ru.ifkbhit.ppo.RxOps._
import ru.ifkbhit.ppo.http.ParsedRequest.BodyNotFound
import rx.lang.scala.Observable

import scala.collection.JavaConverters._
import scala.util.Try

case class ParsedRequest[I](
  path: String,
  queryParams: Map[String, List[String]],
  method: HttpMethod,
  private val body: Observable[I]
) {

  def getBody[T](implicit converter: BodyConverter[I, T]): Observable[T] = {
    body.map(converter)
      .orElse(throw BodyNotFound)
  }

  def isPost: Boolean =
    method == HttpMethod.POST

  def isGet: Boolean =
    method == HttpMethod.GET

  def getIntOpt(name: String): Observable[Option[Int]] =
    RxOps.fromCallable {
      queryParams.get(name)
        .flatMap(_.headOption)
        .map { x =>
          Try(x.toInt)
            .getOrElse(throw new RuntimeException(s"Expected int at `$name`, found $x"))
        }
    }

  def getLongOpt(name: String): Observable[Option[Long]] =
    RxOps.fromCallable {
      queryParams.get(name)
        .flatMap(_.headOption)
        .map { x =>
          Try(x.toLong)
            .getOrElse(throw new RuntimeException(s"Expected long at `$name`, found $x"))
        }
    }

}

object ParsedRequest {

  def parse[I, T](
    httpServerRequest: HttpServerRequest[I],
  ): ParsedRequest[I] = {

    val path = httpServerRequest.getDecodedPath
    val params = httpServerRequest.getQueryParameters.asScala
      .map {
        case (key, values) => key -> values.asScala.toList
      }.toMap

    ParsedRequest[I](
      path = path,
      queryParams = params,
      method = httpServerRequest.getHttpMethod,
      httpServerRequest.getContent.asObservable().asScala
    )
  }

  case object BodyNotFound extends RuntimeException("Body is required!")

}