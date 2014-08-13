package db

import scala.concurrent.ExecutionContext
import org.reactivecouchbase.{CouchbaseBucket, ReactiveCouchbaseDriver}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit


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

  val driver = ReactiveCouchbaseDriver()
  val bucket = driver.bucket("octopus")
}
