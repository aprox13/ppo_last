package ru.ifkbhit.ppo.util

import java.sql.{Connection, DriverManager}

object EventStoreConnectionProvider {


  def get(connectionString: String): Connection = {
    val db = DriverManager.getConnection(connectionString)

    db
  }
}
