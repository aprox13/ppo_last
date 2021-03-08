package ru.ifkbhit.ppo.common.model.response

import spray.json.DefaultJsonProtocol._
import spray.json.{JsValue, JsonFormat, RootJsonWriter}

sealed trait Response {
  def responseCode: Int
}

object Response {

  // result
  private case class FailedResponse(error: String)
  private case class SuccessfulResponse[A](response: A)

  // inner
  private case class FailedStub(msg: String, responseCode: Int) extends Response

  private trait SuccessStub extends Response {
    override def responseCode: Int = 200

    type A

    def response: A

    def jsonFormat: JsonFormat[A]
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

}
