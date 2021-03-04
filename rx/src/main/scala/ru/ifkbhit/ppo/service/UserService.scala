package ru.ifkbhit.ppo.service

import ru.ifkbhit.ppo.model.User
import ru.ifkbhit.ppo.model.response.UsersResponse
import rx.lang.scala.Observable

trait UserService {
  /**
   * Insert single user. If user already exist returns Duplicated exception
   */
  def addUser(user: User): Observable[User]

  /**
   * Fetch single user by id. If user doesn't exist returns NotFound exception.
   */
  def fetchOne(id: Long): Observable[User]

  /**
   * Find user by id
   *
   * @return Some(user), if exists or None
   */
  def find(id: Long): Observable[Option[User]]

  /**
   * Return all user
   */
  def all: Observable[UsersResponse]
}
