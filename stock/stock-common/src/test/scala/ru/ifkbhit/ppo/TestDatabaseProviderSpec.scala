package ru.ifkbhit.ppo

import org.scalatest.{Matchers, WordSpec}
import ru.ifkbhit.ppo.backend.DefaultDatabaseComponents
import ru.ifkbhit.ppo.database.provider.DbConfig
import ru.ifkbhit.ppo.db.PsqlTestContainerProvider

class TestDatabaseProviderSpec extends WordSpec with Matchers with PsqlTestContainerProvider {

  lazy val backend = new DefaultDatabaseComponents {
    override def dbConfig: DbConfig = testDbConfig
  }


  "Database provider" should {
    "correctly provide test db" in {

      backend.database should not be null
    }
  }

}
