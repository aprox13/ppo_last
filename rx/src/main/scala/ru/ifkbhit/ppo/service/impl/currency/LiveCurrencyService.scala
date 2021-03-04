package ru.ifkbhit.ppo.service.impl.currency

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.util.EntityUtils
import ru.ifkbhit.ppo.RxOps
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.model.Currency
import ru.ifkbhit.ppo.service.currency.{Conversion, CurrencyService}
import rx.lang.scala.Observable
import spray.json._

class LiveCurrencyService(apiUrl: String, httpClient: CloseableHttpClient) extends CurrencyService with Logging {
  override def getConversion(base: Currency): Observable[Conversion] =
    RxOps.fromCallable(loadSync(base)).single

  protected def loadSync(base: Currency): Conversion = {
    val currencyCodes = Currency.values.filter(_ != base).map(_.entryName)

    val params: Seq[String] =
      currencyCodes.map(code => s"symbols=$code") ++
        Seq(s"base=${base.entryName}")

    val url = s"$apiUrl?${params.mkString("&")}"

    log.info(s">> $url")
    val responseString = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity)
    log.info(s"<< $responseString")

    responseString.parseJson.convertTo[Conversion]
  }

}
