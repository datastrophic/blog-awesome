package dao

import domain.Comment
import db.CommentBucketClient
import scala.concurrent.Future
import domain.DomainJsonFormats._
import com.couchbase.client.protocol.views.{Stale, ComplexKey, Query}

class CommentDao extends BaseDao[Comment]  with CommentBucketClient {
  def getCommentsByPost(postId: String, skip: Int, limit: Int): Future[List[Comment]] = {
    getReviews(viewName = "by_post", uid = postId, skip, limit)
  }

  def getCommentsByAuthor(authorId: String, skip: Int, limit: Int): Future[List[Comment]] = {
    getReviews(viewName = "by_author", uid = authorId, skip, limit)
  }

  private def getReviews(viewName: String, uid: String, skip: Int, limit: Int): Future[List[Comment]] = {
    executeWithBucket(bucket => {

      val query = new Query()
        .setIncludeDocs(true)
        .setRangeStart(ComplexKey.of(uid,"""{}"""))
        .setRangeEnd(ComplexKey.of(uid).forceArray(true))
        .setInclusiveEnd(true)
        .setDescending(true)
        .setLimit(limit)
        .setSkip(skip)
        .setStale(Stale.FALSE)

      bucket.find[Comment](DesignDocName, viewName)(query)
    })
  }
}
