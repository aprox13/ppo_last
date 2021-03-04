package ru.ifkbhit.ppo.config

import com.typesafe.config.Config
import ru.ifkbhit.ppo.common.config.ConfigBuilder
import ru.ifkbhit.ppo.common.config.ConfigOps._

case class MongoCollections(
  user: String,
  product: String
)

object MongoCollections extends ConfigBuilder[MongoCollections] {

  override def apply(implicit config: Config): MongoCollections =
    new MongoCollections(
      user = str"user",
      product = str"product"
    )
}
