package service

import scala.concurrent.Await
import scala.concurrent.duration._
import dao.CommentDao
import domain.Comment
import play.api.libs.json.{Json, JsError, JsValue}
import util.{StringAndDateUtils, IdGenerator}
import java.util.Date
import play.api.Logger
import metrics.ApplicationMetrics._
import scala.concurrent.ExecutionContext.Implicits.global
import domain.JsonFormats._

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

  def createComment(json: JsValue): Either[String,JsValue] = {
    logger.info("Creating review from JSON")

    val validatedJson = json.validate[Comment]
    validatedJson.fold(
      errors => {
        Left(JsError.toFlatJson(errors).toString())
      },
      comment => {

        val generatedId = IdGenerator.generateUUID
        val dateAsMillis = new Date().getTime
        val displayedDate = StringAndDateUtils.getCurrentDateAsString

        val newComment = comment.copy(id = Some(generatedId), timestamp = Some(dateAsMillis), displayedDate = displayedDate)

        commentDao.save(generatedId, newComment)

        Right(Json.toJson(newComment))
      }
    )
  }

  def updateComment(commentId: String, json: JsValue): Either[String,String] = {
    val validatedJson = json.validate[Comment]
    validatedJson.fold(
      errors => {
        Left(JsError.toFlatJson(errors).toString())
      },
      comment => {
        val dbComment = getById(commentId)
        if(dbComment.isDefined){
          commentDao.save(commentId, dbComment.get.copy(body = comment.body))
          Right(s"Comment with id ${comment.id.get} updated")
        } else {
          Left(s"Comment with id ${comment.id.get} not found!")
        }
      }
    )
  }

  def deleteComment(reviewId: String){
    commentDao.delete(reviewId)
  }

  def getById(key: String) = {
    Await.result(commentDao.get(key), 5 seconds)
  }
  
  def getCommentsByAuthor(uid: String): List[Comment] = ???
}
