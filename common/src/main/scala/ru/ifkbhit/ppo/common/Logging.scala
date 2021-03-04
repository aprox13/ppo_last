package ru.ifkbhit.ppo.common

import org.slf4j.{Logger, LoggerFactory}

trait Logging {

  val log: Logger = LoggerFactory.getLogger(loggerClass)

  protected def loggerClass: Class[_] = this.getClass

  def timed[A](actionName: String)(action: => A): A = {
    log.info(s"Starting $actionName")

    val start = System.currentTimeMillis()
    val res = action
    val end = System.currentTimeMillis() - start

    log.info(s"Done $actionName with ${end} millis")

    res
  }
}
