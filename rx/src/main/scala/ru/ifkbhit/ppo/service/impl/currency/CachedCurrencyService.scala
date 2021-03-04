package ru.ifkbhit.ppo.service.impl.currency

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{Executors, TimeUnit}

import com.google.common.util.concurrent.ThreadFactoryBuilder
import ru.ifkbhit.ppo.RxOps
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.model.Currency
import ru.ifkbhit.ppo.service.currency.{Conversion, CurrencyService}
import rx.lang.scala.Observable
import rx.lang.scala.schedulers.ImmediateScheduler

trait CachedCurrencyService
  extends CurrencyService
    with Logging {

  private val worker = Executors.newScheduledThreadPool(
    1,
    new ThreadFactoryBuilder().setNameFormat(s"cached-currency-%d").build()
  )

  private val data: AtomicReference[Map[Currency, Conversion]] = new AtomicReference()

  protected def delayMinutes: Int

  private def init(): Unit = {
    update() // make it in current thread

    worker.scheduleWithFixedDelay(() => update(),
      delayMinutes,
      delayMinutes,
      TimeUnit.MINUTES
    )
  }

  private def update(): Unit = {

    val toFetch: Seq[Observable[(Currency, Conversion)]] =
      Currency.values.map(c => super.getConversion(c).map(c -> _))

    (for {
      _ <- Observable.just(log.info("Starting load currencies conversions"))
      currencies <- Observable.amb(toFetch: _*).toList.map(_.toMap)
      _ <- Observable.just(log.info("Successfully load currencies conversions"))
    } yield currencies)
      .single
      .subscribeOn(ImmediateScheduler())
      .toBlocking
      .foreach(data.set)
  }


  abstract override def getConversion(base: Currency): Observable[Conversion] = {
    RxOps.fromCallable {
      Option(data.get())
        .getOrElse(throw new RuntimeException("There is no conversion data!"))
        .getOrElse(base, throw new RuntimeException(""))
    }
  }

  init()
}
