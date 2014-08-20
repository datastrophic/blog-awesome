package dao

import db.ReactiveCouchbaseClient
import scala.concurrent.Future

/**
 * Created by akirillov on 8/14/14.
 */
//trait BaseDao[T <: HasId] extends ReactiveCouchbaseClient {
//
//  def save(entity: T) = {
//    executeWithBucket(bucket => bucket.set[T](entity.id, entity))
//  }
//
//  def get(key: String): Future[Option[T]] = {
//    executeWithBucket(bucket => bucket.get[T](key))
//  }
//
//  def delete(key: String) = {
//    executeWithBucket(bucket => bucket.delete(key))
//  }
//
//}
