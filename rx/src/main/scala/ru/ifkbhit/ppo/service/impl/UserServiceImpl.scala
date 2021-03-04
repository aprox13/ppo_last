package ru.ifkbhit.ppo.service.impl

import com.mongodb.client.model.Filters
import ru.ifkbhit.ppo.{Exceptions, RxOps}
import ru.ifkbhit.ppo.model.User
import ru.ifkbhit.ppo.model.response.UsersResponse
import ru.ifkbhit.ppo.service.{DbService, UserService}
import rx.lang.scala.Observable

class UserServiceImpl(dbService: DbService[User]) extends UserService  {
  override def addUser(user: User): Observable[User] =
    dbService.fetchOneOpt(Filters.eq("id", user.id))
      .flatMap {
        case None =>
          dbService.insertOne(user)
        case _ =>
          RxOps.fail(Exceptions.Duplicated("user", user.id))
      }

  override def all: Observable[UsersResponse] =
    dbService.stream(DbService.Paging.All).toList.map(UsersResponse(_))

  override def find(id: Long): Observable[Option[User]] =
    dbService.fetchOneOpt(Filters.eq("id", id))

  override def fetchOne(id: Long): Observable[User] =
    find(id)
      .flatMap {
        case None =>
          RxOps.fail(Exceptions.NotFound(s"user with id `$id`"))
        case Some(value) =>
          Observable.just(value)
      }
}

