package ru.ifkbhit.ppo.http

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.{HttpMethod, HttpResponseStatus}
import io.reactivex.netty.protocol.http.server.{HttpServerRequest, HttpServerResponse, RequestHandler}
import ru.ifkbhit.ppo.Exceptions
import ru.ifkbhit.ppo.Exceptions.BadRequest
import ru.ifkbhit.ppo.RxOps._
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.model.response.Response
import ru.ifkbhit.ppo.common.model.response.Response._
import ru.ifkbhit.ppo.http.BodyConverters.{ByteToStringConverter, _}
import ru.ifkbhit.ppo.http.ParsedRequest.BodyNotFound
import ru.ifkbhit.ppo.model.request.{ProductRequest, ProductsRequest}
import ru.ifkbhit.ppo.model.{StoredProduct, User}
import ru.ifkbhit.ppo.service.{ProductService, UserService, UtilService}
import rx.lang.scala.{Observable, Scheduler}
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.util.matching.Regex

class MainHandler(
  scheduler: Scheduler,
  userService: UserService,
  productService: ProductService,
  utilService: UtilService
) extends RequestHandler[ByteBuf, ByteBuf] with Logging {

  import MainHandler._

  override def handle(
    httpServerRequest: HttpServerRequest[ByteBuf],
    httpServerResponse: HttpServerResponse[ByteBuf]
  ): rx.Observable[Void] = {
    val request = ParsedRequest.parse(httpServerRequest)

    (for {
      response <- process(request)
        .onErrorReturn {
          case x: Exceptions.NotFound =>
            Response.fromThrowable(x, 404)
          case BodyNotFound =>
            Response.fromThrowable(BodyNotFound, 400)
          case x: BadRequest =>
            Response.fromThrowable(x, 400)
          case x =>
            Response.fromThrowable(x)
        }.single
        .subscribeOn(scheduler)
        .observeOn(scheduler)

      res <- httpServerResponse
        .setStatus(HttpResponseStatus.valueOf(response.responseCode))
        .writeString(Observable.just(response).stringify.asJava)
        .asScala

    } yield res).asJava

  }


  private def process(request: ParsedRequest[ByteBuf]): Observable[Response] = {
    request.path match {
      case UsersRoute(_) if request.isGet =>
        userService.all.toResponse

      case ProductsRoute(_) if request.isGet =>
        (for {
          userId <- request.getLongOpt("userId")
          limit <- request.getIntOpt("limit")
            .validate("Limit must be positive") { x => x.forall(_ > 0)}
          result <- productService.fetchProducts(
            ProductsRequest(userId),
            limit
          )
        } yield result).toResponse

      case AddUserRoute(_) if request.isPost =>
        request.getBody[User]
          .flatMap(userService.addUser)
          .toResponse

      case AddProductRoute(_) if request.isPost =>
        request.getBody[StoredProduct]
          .validate("Price must be positive") { _.price.value > 0 }
          .flatMap(productService.addOne)
          .toResponse

      case GetUserRoute(rawId, _) if rawId != null && request.isGet =>
        val id = rawId.toLong

        userService.fetchOne(id).toResponse

      case GetProductRoute(rawId, _) if rawId != null && request.isGet =>
        val id = rawId.toLong

        (for {
          userId <- request.getLongOpt("userId")
          result <- productService.fetchOne(
            ProductRequest(
              id,
              userId
            )
          )
        } yield result).toResponse

      case UtilsDropRoute(collection, _) if collection != null && request.method == HttpMethod.DELETE =>
        utilService.drop(collection)
          .toResponse

      case route =>
        fail[String](Exceptions.NotFound(s"route `${request.method} $route`"))
          .toResponse
    }
  }
}

object MainHandler {

  private val UsersRoute: Regex  = "/users(/)?".r
  private val AddUserRoute: Regex = "/user/add(/)?".r
  private val GetUserRoute: Regex = "/user/(\\d+)(/)?".r
  private val AddProductRoute: Regex = "/product/add(/)?".r
  private val GetProductRoute: Regex = "/product/(\\d+)(/)?".r
  private val ProductsRoute: Regex = "/products(/)?".r
  private val UtilsDropRoute: Regex = "/utils/drop/(\\w+)(/)?".r

  private implicit val UserFromBytes: BodyConverter[ByteBuf, User] =
    ByteToStringConverter.thenParse(_.parseJson.convertTo[User])

  private implicit val ProductFromBytes: BodyConverter[ByteBuf, StoredProduct] =
    ByteToStringConverter.thenParse(_.parseJson.convertTo[StoredProduct])

  implicit class RichResponseObservable[T](val obs: Observable[Response]) extends AnyVal {
    def stringify(implicit jsonWriter: JsonWriter[Response]): Observable[String] =
      obs.map(_.toJson.prettyPrint)
  }

  implicit class ObservableOps[T](val obs: Observable[T]) extends AnyVal {
    def toResponse(implicit format: JsonFormat[T]): Observable[Response] =
      obs.map(x => Response.success(x)(format))

    def validate(msg: String)(f: T => Boolean): Observable[T] =
      obs.map(x => if (f(x)) x else throw BadRequest(msg))
  }

}
