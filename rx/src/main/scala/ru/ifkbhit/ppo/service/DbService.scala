package ru.ifkbhit.ppo.service

import org.bson.conversions.Bson
import rx.lang.scala.Observable

trait DbService[T] {

  /**
   * Find first element by filter
   *
   * @return Some(element), if exists or Observable with NotFound exception
   */
  def fetchOne(filter: Bson): Observable[T]

  /**
   * Find first element by filter
   *
   * @return Some(element), if exists or None
   */
  def fetchOneOpt(filter: Bson): Observable[Option[T]]

  /**
   * Collect all elements with paging
   */
  def stream(implicit paging: DbService.Paging): Observable[T]

  /**
   * Insert single element.
   *
   * @return element itself when success
   */
  def insertOne(element: T): Observable[T]

  /**
   * Drop all element
   */
  def dropAll: Observable[Unit]
}


object DbService {

  case class Paging(
    limit: Option[Int] = None,
    offset: Option[Int] = None
  )

  object Paging {
    val All: Paging = Paging(None, None)
  }

}