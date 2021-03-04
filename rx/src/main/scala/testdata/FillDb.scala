package testdata

import org.apache.http.client.methods.{HttpDelete, HttpEntityEnclosingRequestBase, HttpPost, HttpUriRequest}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.util.EntityUtils
import ru.ifkbhit.ppo.common.BaseApp
import ru.ifkbhit.ppo.common.utils.MapOps._
import ru.ifkbhit.ppo.config.AppConfig
import ru.ifkbhit.ppo.model.{Currency, Price, StoredProduct, User}
import spray.json._

import scala.util._

object FillDb extends BaseApp {

  private def send(
    client: CloseableHttpClient,
  )(httpUriRequest: HttpUriRequest): Unit = {

    val bodyStr = Some(httpUriRequest).collect {
      case r: HttpEntityEnclosingRequestBase =>
        EntityUtils.toString(r.getEntity)
    }.map(s => s"[$s]")

    println(s">> ${httpUriRequest.getMethod} ${httpUriRequest.getURI} ${bodyStr.getOrElse("")}")
    Try(client.execute(httpUriRequest)) match {
      case Success(response) =>
        val result = EntityUtils.toString(response.getEntity)

        println(s"<< ${response.getStatusLine.getStatusCode} $result")
      case Failure(e) =>
        println(s"Error. ${e.getMessage}")
    }
  }

  def toPost[T: JsonFormat](url: String)(body: T): HttpUriRequest =
    new HttpPost(url)
      .applySideEffect(_.setEntity(new StringEntity(body.toJson.compactPrint, ContentType.APPLICATION_JSON)))


  private def choose[T](items: Seq[T]): T =
    items(Random.nextInt(items.size))


  override def appRun(args: String*): Unit = {
    val client = HttpClients.createDefault()
    val cfg = AppConfig(currentConfig)

    val users = Currency.values
      .zipWithIndex
      .map {
        case (currency, i) => User(i + 1, currency)
      }

    val products = (1L to 20).map { id =>
      StoredProduct(
        id = id,
        name = s"product $id",
        price = Price(
          value = 1 + math.abs(Random.nextLong()) % (10 * 10000),
          currency = choose(Currency.values)
        )
      )
    }

    val baseUrl = s"http://localhost:${cfg.httpConfig.port}"

    val addUser = s"$baseUrl/user/add"
    val addProduct = s"$baseUrl/product/add"


    val requests: Seq[HttpUriRequest] =
      Seq(new HttpDelete(s"$baseUrl/utils/drop/all")) ++
        users.map(toPost(addUser)) ++
        products.map(toPost(addProduct))


    requests.foreach(send(client))

    client.close()
  }
}
