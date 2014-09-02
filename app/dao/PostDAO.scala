package dao

import domain.Post
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, ExecutionContext, Await}
import scala.concurrent.duration._
import domain.DomainJsonFormats._
import org.reactivecouchbase.client.OpResult
import play.api.libs.json.{JsObject, Writes, Reads}
import com.couchbase.client.protocol.views.{Stale, ComplexKey, Query}

/**
 * Created by akirillov on 8/14/14.
 */
object PostDAO extends BaseDao[Post]{

  def findPostsByTag(tag: String) = {
    executeWithBucket(bucket =>
      bucket.find[Post]("doc", "by_tag")(new Query().setIncludeDocs(true).setKey(tag).setStale(Stale.FALSE))
    )
  }

  def findDrafts(): Future[List[Post]] = queryDraftsView(isDraft = true)

  def findSubmittedPosts(): Future[List[Post]] = queryDraftsView(isDraft = false)

  private def queryDraftsView(isDraft: Boolean): Future[List[Post]] = {
    executeWithBucket(bucket =>
      bucket.find[Post]("doc", "by_draft")(new Query().setIncludeDocs(true).setKey(isDraft.toString).setStale(Stale.FALSE))
    )
  }

  def exists(uid: String): Boolean = {
    Await.result(get(uid), 5 seconds).isDefined
  }

  def save(key: String, entity: Post) = super.save(key, entity)

  def get(key: String) = super.get(key)

  def delete(key: String) = super.delete(key)
}
