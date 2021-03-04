package ru.ifkbhit.ppo.service.impl

import ru.ifkbhit.ppo.{Exceptions, RxOps}
import ru.ifkbhit.ppo.config.MongoCollections
import ru.ifkbhit.ppo.model.{StoredProduct, User}
import ru.ifkbhit.ppo.service.{DbService, UtilService}
import rx.lang.scala.Observable

class UtilServiceImpl(
  userDb: DbService[User],
  productsDb: DbService[StoredProduct],
  mongoCollections: MongoCollections
) extends UtilService {

  private val collectionsRegistry: Map[String, DbService[_]] =
    Map(
      mongoCollections.user -> userDb,
      mongoCollections.product -> productsDb
    )

  override def drop(collectionName: String): Observable[String] = {
    val name = collectionName.trim
    collectionsRegistry.get(name).map { db =>
      db.dropAll.map(_ => s"Collection `$name` was dropped.")
    }.getOrElse {
      name match {
        case "all" =>
          val drops: Seq[Observable[Unit]] =
            collectionsRegistry.values.toSeq.map(_.dropAll)

          Observable.amb[Unit](drops: _*)
            .map(_ => s"Collections: ${collectionsRegistry.keys.mkString(", ")} was dropped.")
        case otherName =>
          RxOps.fail(Exceptions.NotFound(s"collection `$otherName`"))

      }
    }
  }
}

