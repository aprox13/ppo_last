package ru.ifkbhit.ppo.common.model.response

import spray.json.DefaultJsonProtocol._
import spray.json.{JsValue, JsonFormat, RootJsonFormat, RootJsonReader, RootJsonWriter}

import scala.reflect.ClassTag
import scala.util.Try

sealed trait Response {
  def responseCode: Int
}

object Response {

  // result
  private case class FailedResponse(error: String)

  private case class SuccessfulResponse[A](response: A)

  // inner
  private[response] case class FailedStub(msg: String, responseCode: Int) extends Response

  private[response] trait SuccessStub extends Response {
    override def responseCode: Int = 200

    type A

    def response: A

    def jsonFormat: JsonFormat[A]

    override def equals(o: Any): Boolean =
      o match {
        case s: SuccessStub if s.response == response =>
          true
        case _ =>
          false
      }
  }

  // support
  def success[T](r: T)(implicit format: JsonFormat[T]): Response = new SuccessStub {
    override type A = T

    override def response: A = r

    override def jsonFormat: JsonFormat[A] = format

    override def responseCode: Int = 200
  }

  def failed(msg: String, code: Int = 500): Response = FailedStub(msg, code)

  def fromThrowable(t: Throwable, code: Int = 500): Response =
    failed(t.getMessage, code)


  // converts
  private def failedFormat = jsonFormat1(FailedResponse)

  private def successfulResponseFormat[A](implicit f: JsonFormat[A]) = jsonFormat1(SuccessfulResponse[A])


  class ResponseReaderFormat[T: JsonFormat] extends RootJsonReader[T] {
    override def read(json: JsValue): T = {
      val error = Try(failedFormat.read(json))
        .map(_.error)

      if (error.isSuccess) throw FailedResponseException(error.get)
      else successfulResponseFormat[T].read(json).response
    }
  }


  implicit object ResponseJsonFormat extends RootJsonWriter[Response] {

    override def write(obj: Response): JsValue = {
      obj match {
        case response: SuccessStub =>
          val stub = SuccessfulResponse(response.response)
          successfulResponseFormat(response.jsonFormat).write(stub)

        case x: FailedStub =>
          failedFormat.write(FailedResponse(x.msg))
      }
    }
  }

  object WriteOnlyJsonFormat extends RootJsonFormat[Response] {
    override def write(obj: Response): JsValue = ResponseJsonFormat.write(obj)

    override def read(json: JsValue): Response = throw new UnsupportedOperationException("format is read only")
  }

  implicit class ResponseOps(val response: Response) extends AnyVal {

    def as[T: ClassTag]: T = {
      response match {
        case successStub: SuccessStub if successStub.response.isInstanceOf[T] =>
          successStub.response.asInstanceOf[T]
        case x =>
          throw new RuntimeException(s"Unexpected response $x")
      }


    }

  }


  case class FailedResponseException(msg: String) extends RuntimeException(msg)

}
