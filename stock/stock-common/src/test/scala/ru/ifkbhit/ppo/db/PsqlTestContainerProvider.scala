package ru.ifkbhit.ppo.db

import org.testcontainers.containers.PostgreSQLContainer
import ru.ifkbhit.ppo.common.model.config.HttpEndpoint
import ru.ifkbhit.ppo.database.provider.DbConfig

trait PsqlTestContainerProvider {

  private val image = "postgres:9.6.12"
  private val dbConfig: DbConfig = DbConfig(
    name = "stock_tc",
    endpoint = HttpEndpoint(
      "localhost",
      PostgreSQLContainer.POSTGRESQL_PORT,
      schema = "jdbc:postgresql"
    ),
    user = "tc-user",
    password = Some("tc-pass"),
    maxConnections = Some(10),
    createSchema = true
  )

  protected val container: PostgreSQLContainer[PsqlContainer] =
    new PsqlContainer()
      .withUsername(dbConfig.user)
      .withDatabaseName(dbConfig.name)
      .withPassword(dbConfig.password.get)

  class PsqlContainer extends PostgreSQLContainer[PsqlContainer](image)

  protected def testDbConfig: DbConfig = {
    val port = container.getMappedPort(dbConfig.endpoint.port)
    val host = container.getContainerIpAddress

    val endpoint = dbConfig.endpoint
      .copy(host = host, port = port)

    dbConfig.copy(endpoint = endpoint)
  }

  container.start()

  sys.addShutdownHook(container.stop())
}
