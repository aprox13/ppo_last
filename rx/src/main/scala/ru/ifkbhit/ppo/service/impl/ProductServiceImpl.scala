package ru.ifkbhit.ppo.service.impl

import com.mongodb.client.model.Filters
import ru.ifkbhit.ppo.common.Logging
import ru.ifkbhit.ppo.model.StoredProduct
import ru.ifkbhit.ppo.model.request.{ProductRequest, ProductsRequest}
import ru.ifkbhit.ppo.model.response.{ProductResponse, ProductsResponse}
import ru.ifkbhit.ppo.service.currency.CurrencyService
import ru.ifkbhit.ppo.service.{DbService, ProductService, UserService}
import ru.ifkbhit.ppo.{Exceptions, RxOps}
import rx.lang.scala.Observable

class ProductServiceImpl(currencyService: CurrencyService, dbService: DbService[StoredProduct], userService: UserService) extends ProductService with Logging {

  private def fetchWithUser(userId: Option[Long])(productOrStream: => Observable[StoredProduct]): Observable[StoredProduct] = {
    userId.map(userService.find)
      .getOrElse(Observable.just(None))
      .flatMap {
        case None =>
          productOrStream

        case Some(user) =>
          for {
            conversions <- currencyService.getConversion(user.currency)
            result <- productOrStream
              .map { product =>
                conversions.convert(product.price)
                  .map(p => product.copy(price = p))
              }
              .collect { case Some(p) => p }

          } yield result
      }

  }


  override def fetchOne(productRequest: ProductRequest): Observable[ProductResponse] =
    fetchWithUser(productRequest.userId) {
      dbService.fetchOne(Filters.eq("id", productRequest.id))
        .onErrorResumeNext {
          case Exceptions.NotFound(_) =>
            RxOps.fail(Exceptions.NotFound(s"product with id `${productRequest.id}`"))
          case t =>
            RxOps.fail(t)
        }
    }.map(ProductResponse.fromStored)


  override def fetchProducts(productsRequest: ProductsRequest, limit: Option[Int]): Observable[ProductsResponse] =
    fetchWithUser(productsRequest.userId) {
      dbService.stream(DbService.Paging(limit = limit))
    }
      .map(ProductResponse.fromStored)
      .toList.map(ProductsResponse(_))


  override def addOne(product: StoredProduct): Observable[ProductResponse] =
    dbService.fetchOneOpt(Filters.eq("id", product.id))
      .flatMap {
        case None =>
          dbService.insertOne(product)
            .map(ProductResponse.fromStored)
        case _ =>
          RxOps.fail(Exceptions.Duplicated("product", product.id))
      }
}

