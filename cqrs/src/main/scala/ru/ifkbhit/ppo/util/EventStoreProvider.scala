package ru.ifkbhit.ppo.util

import java.io.{BufferedReader, InputStreamReader}
import java.sql.{Connection, DriverManager}
import java.util.stream.Collectors

object EventStoreProvider {

  private def getInitSql: String =
    new BufferedReader(new InputStreamReader(this.getClass.getClassLoader.getResourceAsStream("init.sql")))
      .lines()
      .collect(Collectors.joining(System.lineSeparator()))

  def get(connectionString: String): Connection = {
    val db = DriverManager.getConnection(connectionString)

    //    db.createStatement().execute(getInitSql)
    db
  }
}
