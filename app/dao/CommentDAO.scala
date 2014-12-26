package dao

import domain.Comment
import db.CommentBucketClient
import scala.concurrent.Future
import domain.JsonFormats._
import com.couchbase.client.protocol.views.{Stale, ComplexKey, Query}

class CommentDao extends BaseDao[Comment]  with CommentBucketClient {
  def getCommentsByPost(postId: String, skip: Int, limit: Int): Future[List[Comment]] = {
    getComments(viewName = "by_post", uid = postId, skip, limit)
  }

  def getCommentsByAuthor(authorId: String, skip: Int, limit: Int): Future[List[Comment]] = {
    getComments(viewName = "by_author", uid = authorId, skip, limit)
  }

  private def getComments(viewName: String, uid: String, skip: Int, limit: Int): Future[List[Comment]] = {
    executeWithBucket(bucket => {

      val query = new Query()
        .setIncludeDocs(true)
        .setRangeStart(ComplexKey.of(uid))
        .setRangeEnd(ComplexKey.of(uid,"""{}"""))
        .setInclusiveEnd(true)
        .setLimit(limit)
        .setSkip(skip)
        .setStale(Stale.FALSE)

      bucket.find[Comment](DesignDocName, viewName)(query)
    })
  }
}
