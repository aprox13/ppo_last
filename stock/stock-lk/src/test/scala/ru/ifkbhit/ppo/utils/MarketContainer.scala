package ru.ifkbhit.ppo.utils

import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.database.provider.DbConfig

import scala.collection.JavaConverters._

class MarketContainer extends GenericContainer[MarketContainer]("market:1.0")
  with Logging {

  def withLocalDbConfig(dbConfig: DbConfig): MarketContainer =
    this.withEnv(
      (Map(
        "STOCK_DB_HOST" -> "host.docker.internal",
        "STOCK_DB_PORT" -> dbConfig.endpoint.port.toString,
        "STOCK_DB_NAME" -> dbConfig.name,
        "STOCK_DB_USER" -> dbConfig.user
      ) ++ dbConfig.password.map("STOCK_DB_PASSWORD" -> _).toMap).asJava
    )

  override def start(): Unit = {

    log.info(s"Starting container with image $getDockerImageName")
    getEnvMap.forEach {
      (k, v) => log.info(s"[ENV] $k=$v")
    }

    super.start()


    getExposedPorts.forEach { p =>

      log.info(s"Exposed: ${getMappedPort(p)} -> $p")
    }
  }
}


class BasicMarketContainer(apiPort: Int, dbConfig: DbConfig) extends MarketContainer {
  withLocalDbConfig(dbConfig)
    .withEnv("STOCK_MARKET_API_PORT", apiPort.toString)
    .withExposedPorts(apiPort)
    .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("market-docker")))

  def getMappedApiPort: Int =
    getMappedPort(apiPort)
}