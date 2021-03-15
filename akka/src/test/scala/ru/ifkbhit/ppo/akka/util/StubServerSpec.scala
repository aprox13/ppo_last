package ru.ifkbhit.ppo.akka.util

import com.xebialabs.restito.builder.stub.StubHttp.whenHttp
import com.xebialabs.restito.semantics.Action._
import com.xebialabs.restito.semantics.Condition._
import com.xebialabs.restito.server.StubServer
import org.glassfish.grizzly.http.Method
import org.glassfish.grizzly.http.util.HttpStatus
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import ru.ifkbhit.ppo.akka.model.SearchResponse
import spray.json.enrichAny

import scala.collection.mutable

class StubServerSpec extends WordSpec with BeforeAndAfterAll with TestEngines {
  private val servers: mutable.Map[Int, StubServer] = mutable.Map.empty[Int, StubServer]

  def registerStubs(engines: TestEngine*): Unit = {
    engines.map(_.port)
      .foreach { port =>
        servers(port) = new StubServer(port)
      }
  }

  def server(engine: TestEngine): StubServer =
    servers(engine.port)

  override def beforeAll(): Unit = {
    servers.values.foreach(_.run())
  }

  override def afterAll(): Unit = servers.values.foreach(_.stop())

}

object SearchStubServerSupport {

  implicit class StubServerOps(val server: StubServer) extends AnyVal {

    private def basicCall =
      whenHttp(server)
        .`match`(method(Method.GET))

    def shouldTimeout: Unit =
      basicCall.`then`(delay(10 * 1000),
        status(HttpStatus.OK_200),
        stringContent(SearchResponse("long search").toJson.compactPrint),
        header("Content-Type", "application/json")
      )


    def shouldResponse(searchResponse: SearchResponse): Unit =
      basicCall
        .`then`(
          status(HttpStatus.OK_200),
          stringContent(searchResponse.toJson.compactPrint),
          header("Content-Type", "application/json")
        )
  }

}