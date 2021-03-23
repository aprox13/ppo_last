package ru.ifkbhit.ppo.backend

import ru.ifkbhit.ppo.database.currentProfile
import ru.ifkbhit.ppo.database.currentProfile.api._
import ru.ifkbhit.ppo.database.provider.{DatabaseProvider, DbConfig}

trait DatabaseComponents {

  val database: Database

  def dbConfig: DbConfig
}


trait DefaultDatabaseComponents extends DatabaseComponents {

  override lazy val database: currentProfile.api.Database = {
    val result = DatabaseProvider.provide(dbConfig)

    sys.addShutdownHook(result.close())

    result
  }
}