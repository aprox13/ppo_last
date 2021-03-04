package ru.ifkbhit.ppo.common


import ru.ifkbhit.ppo.common.config.ConfigSupport

import scala.util.control.NonFatal

class BaseApp extends Logging with ConfigSupport {

  def appRun(args: String*): Unit = ()

  def main(args: Array[String]): Unit = {
    try {
      appRun(args: _*)
    } catch {
      case NonFatal(x) =>
        x.printStackTrace()
        System.exit(1)
    }
  }
}

