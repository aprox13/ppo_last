package ru.ifkbhit.ppo

import java.util.concurrent.Executors

import ru.ifkbhit.ppo.backend.ExecutionContextComponents

import scala.concurrent.ExecutionContext

trait TestExecutionContextComponents extends ExecutionContextComponents {

  protected def pools: Int = 4

  override implicit def ec: ExecutionContext =
    ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(pools)
    )

}
