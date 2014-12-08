package db

import scala.concurrent.ExecutionContext
import org.reactivecouchbase.{CouchbaseBucket, ReactiveCouchbaseDriver}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import com.typesafe.config.ConfigFactory

trait ReactiveCouchbaseClient {
  implicit val ec = ExecutionContext.Implicits.global
  val timeout = Duration(10, TimeUnit.SECONDS)

  val driver = ReactiveCouchbaseDriver()
  val config = ConfigFactory.load()

  def bucketName: String

  val DesignDocName = "blog_mr"
  def bucket: CouchbaseBucket = driver.bucket(bucketName)

  def executeWithBucket[T](function: CouchbaseBucket => T) = {
    function(bucket)
  }
}

trait UserBucketClient extends ReactiveCouchbaseClient{
  override def bucketName: String = config.getString("couchbase.user.bucket")
}

trait PostBucketClient extends ReactiveCouchbaseClient{
  override def bucketName: String = config.getString("couchbase.post.bucket")
}

trait TagBucketClient extends ReactiveCouchbaseClient{
  override def bucketName: String = config.getString("couchbase.tag.bucket")
}

trait CommentBucketClient extends ReactiveCouchbaseClient{
  override def bucketName: String = config.getString("couchbase.comment.bucket")
}
