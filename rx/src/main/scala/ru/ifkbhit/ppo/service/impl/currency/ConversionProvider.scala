package ru.ifkbhit.ppo.service.impl.currency

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.util.EntityUtils
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.common.provider.Provider
import ru.ifkbhit.ppo.model.Currency
import ru.ifkbhit.ppo.service.currency.Conversion
import spray.json._

class ConversionProvider(apiUrl: String, httpClient: CloseableHttpClient)
  extends Provider[Map[Currency, Conversion]]
    with Logging {

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

  override def get: Map[Currency, Conversion] =
    Currency.values
      .map { base =>
        base -> loadSync(base)
      }
      .toMap
}
