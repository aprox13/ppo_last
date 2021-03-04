package ru.ifkbhit.ppo.akka.util

import com.xebialabs.restito.server.StubServer
import org.scalatest.{BeforeAndAfter, WordSpec}

import scala.collection.mutable

class StubServerSpec extends WordSpec with BeforeAndAfter {
  private val servers: mutable.Map[Int, StubServer] = mutable.Map.empty[Int, StubServer]

  def registerStub(port: Int): Unit = {
    servers(port) = new StubServer(port)
  }

  def server(port: Int): StubServer =
    servers(port)

  before {
    servers.values.foreach(_.run())
  }

  after {
    servers.values.foreach(_.stop())
  }

}
