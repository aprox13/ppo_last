package ru.ifkbhit.ppo.service.currency

import ru.ifkbhit.ppo.model.Currency
import rx.lang.scala.Observable

trait CurrencyService {

  /**
   * Getting conversions from `base` currency
   */
  def getConversion(base: Currency): Observable[Conversion]
}

