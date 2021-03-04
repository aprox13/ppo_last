package ru.ifkbhit.ppo.service

import ru.ifkbhit.ppo.model.StoredProduct
import ru.ifkbhit.ppo.model.request.{ProductRequest, ProductsRequest}
import ru.ifkbhit.ppo.model.response.{ProductResponse, ProductsResponse}
import rx.lang.scala.Observable

trait ProductService {

  /**
   * Fetch single product by id. If product doesn't exist returns NotFound exception.
   *
   * If productRequest.userId is defined, return transformed for user price
   */
  def fetchOne(productRequest: ProductRequest): Observable[ProductResponse]

  /**
   * Fetch products with limit.
   *
   * If productsRequest.userId is defined, return transformed for user prices
   */
  def fetchProducts(productsRequest: ProductsRequest, limit: Option[Int]): Observable[ProductsResponse]

  /**
   * Insert single product. If product already exist returns Duplicated exception
   */
  def addOne(product: StoredProduct): Observable[ProductResponse]
}
