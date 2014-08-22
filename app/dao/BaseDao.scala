package dao

import db.ReactiveCouchbaseClient
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.{Reads, Writes}

/**
 * Created by akirillov on 8/14/14.
 */
trait BaseDao[T] extends ReactiveCouchbaseClient {

  def save(key: String, entity: T)(implicit w: Writes[T], ec: ExecutionContext) = {
    executeWithBucket(bucket => bucket.set[T](key, entity))
  }

  def get(key: String)(implicit w: Reads[T], ec: ExecutionContext): Future[Option[T]] = {
    executeWithBucket(bucket => bucket.get[T](key))
  }

  def delete(key: String)(implicit ec: ExecutionContext) = {
    executeWithBucket(bucket => bucket.delete(key))
  }

}
