package dao

import domain.{ViewPage, Post}
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import com.couchbase.client.protocol.views.{Stale, ComplexKey, Query}
import db.PostBucketClient
import domain.DomainJsonFormats._


class PostDao extends BaseDao[Post] with PostBucketClient{

  /**
   * Only published posts are shown, drafts are ignored (see by_tag couchbase view)
   */
  def findPostsByTag(tag: String, pageNum: Int): Future[List[Post]] = {
    executeWithBucket(bucket => {

      val query = new Query()
        .setIncludeDocs(true)
        .setRangeStart(ComplexKey.of(tag,"""{}"""))
        .setRangeEnd(ComplexKey.of(tag).forceArray(true))
        .setInclusiveEnd(true)
        .setDescending(true)
        .setLimit(ViewPage.PageSize)
        .setSkip(ViewPage.PageSize * (pageNum-1))
        .setStale(Stale.FALSE)

        bucket.find[Post](DesignDocName, "by_tag")(query)
    })
  }

  def findDrafts(pageNum: Int): Future[List[Post]] = queryDraftsView(isDraft = true, pageNum)

  def findSubmittedPosts(pageNum: Int): Future[List[Post]] = queryDraftsView(isDraft = false, pageNum)

  private def queryDraftsView(isDraft: java.lang.Boolean, pageNum: Int): Future[List[Post]] = {
    executeWithBucket(bucket => {

      val query = new Query()
        .setIncludeDocs(true)
        .setRangeStart(ComplexKey.of(isDraft, """{}"""))
        .setRangeEnd(ComplexKey.of(isDraft).forceArray(true))
        .setInclusiveEnd(true)
        .setDescending(true)
        .setLimit(ViewPage.PageSize)
        .setSkip(ViewPage.PageSize * (pageNum-1))
        .setStale(Stale.FALSE)

      bucket.find[Post](DesignDocName, "by_draft")(query)
    })
  }

  def exists(uid: String): Boolean = {
    Await.result(get(uid), 5 seconds).isDefined
  }
}
