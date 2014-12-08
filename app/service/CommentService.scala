package service

import scala.concurrent.Await
import scala.concurrent.duration._
import dao.CommentDao
import domain.Comment
import play.api.libs.json.{JsError, JsValue}
import util.IdGenerator
import java.util.Date
import play.api.Logger
import metrics.ApplicationMetrics._
import scala.concurrent.ExecutionContext.Implicits.global
import domain.DomainJsonFormats._

class CommentService(commentDao: CommentDao) {

  private val logger = Logger("[PostService]")

  def getCommentsByPostId(uid: String): List[Comment] = {
    logger.info(s"Attempting to get post by id [$uid]")
    val context = couchbaseReadTime.time()

    val comments = Await.result(commentDao.getCommentsByPost(uid, skip = 0, limit = 100), 5 seconds)

    logger.info(s"Comments lookup by id [$uid]. Found: ${comments.size}")
    couchbaseReadRequests.mark()
    context.stop()

    comments
  }

  def createComment(json: JsValue): Either[String,String] = {
    logger.info("Creating review from JSON")

    val validatedJson = json.validate[Comment]
    validatedJson.fold(
      errors => {
        Left(JsError.toFlatJson(errors).toString())
      },
      comment => {

        val generatedId = IdGenerator.generateUUID
        val dateAsMillis = new Date().getTime

        val newComment = comment.copy(id = Some(generatedId), timestamp = Some(dateAsMillis))

        commentDao.save(generatedId, newComment)

        Right(generatedId)
      }
    )
  }

  def updateComment(reviewId: String, json: JsValue): Either[String,String] = {
    val validatedJson = json.validate[Comment]
    validatedJson.fold(
      errors => {
        Left(JsError.toFlatJson(errors).toString())
      },
      review => {
        if(exists(reviewId)){
          commentDao.save(reviewId, review)
          Right(s"Comment with id ${review.id.get} updated")
        } else {
          Left(s"Comment with id ${review.id.get} not found!")
        }
      }
    )
  }

  def deleteComment(reviewId: String){
    commentDao.delete(reviewId)
  }

  def exists(key: String): Boolean = {
    Await.result(commentDao.get(key), 5 seconds).isDefined
  }
  
  def getCommentsByAuthor(uid: String): List[Comment] = ???
}
