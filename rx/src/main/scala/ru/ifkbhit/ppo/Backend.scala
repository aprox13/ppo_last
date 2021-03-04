package ru.ifkbhit.ppo

import java.util.concurrent.Executors

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.mongodb.rx.client.MongoClients
import org.apache.http.impl.client.HttpClients
import ru.ifkbhit.ppo.config.AppConfig
import ru.ifkbhit.ppo.model.{StoredProduct, User}
import ru.ifkbhit.ppo.service._
import ru.ifkbhit.ppo.service.currency.CurrencyService
import ru.ifkbhit.ppo.service.impl.currency.{CachedCurrencyService, LiveCurrencyService}
import ru.ifkbhit.ppo.service.impl.{MongoDbService, ProductServiceImpl, UserServiceImpl, UtilServiceImpl}
import rx.lang.scala.Scheduler
import rx.lang.scala.schedulers.ExecutionContextScheduler

import scala.concurrent.ExecutionContext

case class Backend(
  scheduler: Scheduler,
  userService: UserService,
  productService: ProductService,
  utilService: UtilService
)

object BackendBuilder {

  def build(appConfig: AppConfig): Backend = {
    lazy val scheduler: Scheduler = ExecutionContextScheduler(
      ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(
          appConfig.schedulerConfig.pools,
          new ThreadFactoryBuilder().setNameFormat("api-scheduler-%d").build()
        )
      )
    )

    val mongoDb = MongoClients.create(appConfig.mongoConfig.connectionString)
      .getDatabase(appConfig.mongoConfig.database)

    val userDb: DbService[User] =
      new MongoDbService[User](mongoDb, appConfig.mongoCollections.user)
    lazy val userService: UserService =
      new UserServiceImpl(userDb)

    val productDb: DbService[StoredProduct] =
      new MongoDbService[StoredProduct](mongoDb, appConfig.mongoCollections.product)

    val currencyService: CurrencyService =
      new LiveCurrencyService(appConfig.currencyApiUrl, HttpClients.createDefault()) with CachedCurrencyService {
        override protected def delayMinutes: Int = 1
      }

    val productService: ProductService =
      new ProductServiceImpl(currencyService, productDb, userService)


    val utilService: UtilService =
      new UtilServiceImpl(userDb, productDb, appConfig.mongoCollections)

    Backend(
      scheduler = scheduler,
      userService = userService,
      productService = productService,
      utilService = utilService
    )
  }
}