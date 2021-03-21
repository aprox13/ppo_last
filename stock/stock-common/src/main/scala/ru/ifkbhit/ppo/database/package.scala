package ru.ifkbhit.ppo

import ru.ifkbhit.ppo.model.Money


package object database {
  val currentProfile = slick.jdbc.PostgresProfile

  import currentProfile.api._

  type QT[T <: Table[_]] = TableQuery[T]

  type DBRead[T] = DBIOAction[T, NoStream, Effect.Read]
  type DBWrite[T] = DBIOAction[T, NoStream, Effect.Write]
  type DBReadWrite[T] = DBIOAction[T, NoStream, Effect.Read with Effect.Write]

  implicit val priceType: BaseColumnType[Money] =
    MappedColumnType.base[Money, Long](
      _.pennies,
      Money.apply
    )


  private[database] def requireAction(boolean: Boolean, throwable: => Throwable): DBIOAction[Unit, NoStream, Effect] =
    if (boolean) DBIO.successful[Unit](()) else DBIO.failed(throwable)
}
