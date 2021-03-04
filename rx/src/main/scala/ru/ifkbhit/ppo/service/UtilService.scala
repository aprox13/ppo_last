package ru.ifkbhit.ppo.service

import rx.lang.scala.Observable

trait UtilService {

  /**
   * Drop collection by name
   * @param collectionName - name of collection or `all`
   */
  def drop(collectionName: String): Observable[String]
}
