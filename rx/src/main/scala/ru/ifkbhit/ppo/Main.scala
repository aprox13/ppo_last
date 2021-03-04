package ru.ifkbhit.ppo

import io.reactivex.netty.protocol.http.server.HttpServer
import ru.ifkbhit.ppo.common.{BaseApp, Logging}
import ru.ifkbhit.ppo.config.AppConfig
import ru.ifkbhit.ppo.http.MainHandler


object Main extends BaseApp with Logging {

  private val appConfig = AppConfig(currentConfig)


  override def appRun(args: String*): Unit = {
    val backend = BackendBuilder.build(appConfig)

    val handler = new MainHandler(
      backend.scheduler,
      backend.userService,
      backend.productService,
      backend.utilService
    )

    HttpServer.newServer(appConfig.httpConfig.port)
      .start(handler)
      .awaitShutdown()
  }

}
