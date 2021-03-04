package ru.ifkbhit.ppo.service.impl

import com.mongodb.rx.client.{FindObservable, MongoDatabase}
import org.bson.Document
import org.bson.conversions.Bson
import ru.ifkbhit.ppo.Exceptions
import ru.ifkbhit.ppo.RxOps._
import ru.ifkbhit.ppo.model.format.DocFormat
import ru.ifkbhit.ppo.service.DbService
import rx.lang.scala.Observable
import ru.ifkbhit.ppo.common.utils.MapOps._

class MongoDbService[T](
  database: MongoDatabase,
  collectionName: String
)(implicit val docFormat: DocFormat[T]) extends DbService[T] {

  import MongoDbService._

  private def collection = database.getCollection(collectionName)

  override def fetchOne(filter: Bson): Observable[T] =
    fetchOneOpt(filter)
      .map {
        case None =>
          throw Exceptions.NotFound(s"$collectionName/fetchOne($filter)")
        case Some(value) =>
          value
      }

  override def fetchOneOpt(filter: Bson): Observable[Option[T]] =
    collection.find(filter)
      .fetchResult
      .map(Some.apply)
      .orElse(None)
      .single

  override def insertOne(element: T): Observable[T] =
    collection
      .insertOne(docFormat.write(element))
      .asScala
      .map(_ => element)

  override def stream(implicit paging: DbService.Paging): Observable[T] =
    collection.find()
      .applyTransformIf(paging.limit.isDefined) { _.limit(paging.limit.get) }
      .applyTransformIf(paging.offset.isDefined) { _.skip(paging.offset.get) }
      .fetchResult

  override def dropAll: Observable[Unit] =
    collection.drop().asObservable()
      .asScala
      .map(_ => ())
}

object MongoDbService {

  implicit class FindOps(val fObs: FindObservable[Document]) extends AnyVal {

    def fetchResult[T](implicit docF: DocFormat[T]): Observable[T] =
      fObs.toObservable.asScala.map(docF.read)
  }

}
