package ru.ifkbhit.ppo.service.impl.currency

import ru.ifkbhit.ppo.RxOps
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.provider.Provider
import ru.ifkbhit.ppo.model.Currency
import ru.ifkbhit.ppo.service.currency.{Conversion, CurrencyService}
import rx.lang.scala.Observable

class LiveCurrencyService(conversionMapProvider: Provider[Map[Currency, Conversion]]) extends CurrencyService with Logging {
  override def getConversion(base: Currency): Observable[Conversion] =
    RxOps.fromCallable {
      conversionMapProvider.get
        .getOrElse(base, throw new RuntimeException(s"There is no conversion data for $base"))

    }
}
