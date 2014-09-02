package dao

import domain.Post
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, ExecutionContext, Await}
import scala.concurrent.duration._
import domain.DomainJsonFormats._
import org.reactivecouchbase.client.OpResult
import play.api.libs.json.{Json, JsObject, Writes, Reads}
import com.couchbase.client.protocol.views.{Stale, ComplexKey, Query}

/**
 * Created by akirillov on 8/14/14.
 */
object PostDAO extends BaseDao[Post]{

  /**
   * Only published posts are shown, drafts are ignored (see by_tag couchbase view)
   */
  def findPostsByTag(tag: String) = {
    executeWithBucket(bucket => {

      val query = new Query()
        .setIncludeDocs(true)
        .setRangeStart(ComplexKey.of(tag,"""{}"""))
        .setRangeEnd(ComplexKey.of(tag).forceArray(true))
        .setInclusiveEnd(true)
        .setDescending(true)

      bucket.find[Post]("doc", "by_tag")(query)
    })
  }

  def findDrafts(): Future[List[Post]] = queryDraftsView(isDraft = true)

  def findSubmittedPosts(): Future[List[Post]] = queryDraftsView(isDraft = false)

  private def queryDraftsView(isDraft: java.lang.Boolean): Future[List[Post]] = {
    executeWithBucket(bucket => {

      val query = new Query()
        .setIncludeDocs(true)
        .setRangeStart(ComplexKey.of(isDraft, """{}"""))
        .setRangeEnd(ComplexKey.of(isDraft).forceArray(true))
        .setInclusiveEnd(true)
        .setDescending(true)

      bucket.find[Post]("doc", "by_draft")(query)
    })
  }

  def exists(uid: String): Boolean = {
    Await.result(get(uid), 5 seconds).isDefined
  }

  def save(key: String, entity: Post) = super.save(key, entity)

  def get(key: String) = super.get(key)

  def delete(key: String) = super.delete(key)
}
