package ru.ifkbhit.ppo.common.model.response

import org.scalatest.matchers.{BeMatcher, MatchResult}

trait ResponseMatcher {

  val failedResponse: BeMatcher[Response] = new FailedMatcherWord(None)

  def failedResponse(msg: String): BeMatcher[Response] = new FailedMatcherWord(Some(msg))

  def successfulResponse[T](entry: T): BeMatcher[Response] = new SuccessfulMatcherWord[T](Some(entry))

  val successfulResponse: BeMatcher[Response] = new SuccessfulMatcherWord[Nothing](None)

  private class FailedMatcherWord(message: Option[String]) extends BeMatcher[Response] {
    override def apply(left: Response): MatchResult =
      left match {
        case Response.FailedStub(msg, _) =>
          MatchResult(
            message.forall(_ == msg),
            s"Response was failed with message '$msg'",
            s"Failed response"
          )
        case stub: Response.SuccessStub =>
          MatchResult(
            matches = false,
            s"Got successful response with entry ${stub.response}",
            "Failed response"
          )
      }
  }

  private class SuccessfulMatcherWord[T](entry: Option[T]) extends BeMatcher[Response] {
    override def apply(left: Response): MatchResult =
      left match {
        case Response.FailedStub(msg, _) =>
          MatchResult(
            matches = false,
            s"Failed response with message '$msg'",
            s"Successful response"
          )
        case stub: Response.SuccessStub =>
          val failedMsg = if (entry.isDefined) {
            s"Expected successful with entry ${entry.get}, but found with ${stub.response}"
          } else {
            s"Expected successful"
          }

          MatchResult(
            matches = entry.forall(_ == stub.response),
            failedMsg,
            s"Successful with entry $entry"
          )

      }


  }

}

object ResponseMatcher extends ResponseMatcher


