package db

import scala.concurrent.ExecutionContext
import org.reactivecouchbase.{CouchbaseBucket, ReactiveCouchbaseDriver}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import play.api.{Play, Configuration}
import com.typesafe.config.ConfigFactory


/**
 * Created by akirillov on 8/13/14.
 */

trait ReactiveCouchbaseClient {
  import ReactiveCouchbaseClient._

  def executeWithBucket[T](function: CouchbaseBucket => T) = {
    function(bucket)
  }
}

object ReactiveCouchbaseClient {
  implicit val ec = ExecutionContext.Implicits.global
  val timeout = Duration(10, TimeUnit.SECONDS)

  val config = ConfigFactory.load()

  val driver = ReactiveCouchbaseDriver()
  val bucketName = config.getString("couchbase.default.bucket")
  val bucket = driver.bucket(bucketName)
}
