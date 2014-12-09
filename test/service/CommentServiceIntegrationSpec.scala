package service

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, Matchers, FunSpec}
import scala.collection.mutable
import java.util.Date
import play.api.libs.json.Json
import scala.concurrent.Await
import util.{DomainEntityGenerator, SpringContextHelper}
import dao.CommentDao
import domain.Comment
import scala.concurrent.duration._
import domain.JsonFormats._
import scala.concurrent.ExecutionContext.Implicits.global

@RunWith(classOf[JUnitRunner])
class CommentServiceIntegrationSpec extends FunSpec with Matchers with BeforeAndAfterAll{

  val context = SpringContextHelper.springContext

  val commentDao: CommentDao = context.getBean(classOf[CommentDao])
  val commentService: CommentService = context.getBean(classOf[CommentService])

  private val keys = mutable.Queue.empty[String]

  describe("CommentService"){

    it("correctly retrieves comments by post") {

      val postId = "test_post_id"
      val comments = (1 to 10).map(num => DomainEntityGenerator.createComment(s"uid_$num", postId))

      comments foreach { review =>
        val time = new Date().getTime
        saveComment(review.copy(timestamp = Some(time)))
        Thread.sleep(2)
      }

      val foundComments = commentService.getCommentsByPostId(postId)

      foundComments.isEmpty shouldEqual false
      foundComments.size shouldEqual 10

      foundComments.head.postId shouldEqual postId
      foundComments.last.postId shouldEqual postId

      comments foreach deleteComment

      val emptyComments = commentService.getCommentsByPostId(postId)

      emptyComments.isEmpty shouldEqual true
    }
    it("provide proper ordering by date") {
      val postId = "test_post_id"
      val comments = (1 to 21).map(num => DomainEntityGenerator.createComment(s"uid_$num", postId))

      comments foreach { review =>
        val time = new Date().getTime
        saveComment(review.copy(timestamp = Some(time)))
        Thread.sleep(2)
      }

      val foundComments = commentService.getCommentsByPostId(postId)

      foundComments.isEmpty shouldEqual false
      foundComments.size shouldEqual 21
      foundComments.head.id shouldEqual comments.last.id //saved last shown first
      foundComments.last.id shouldEqual comments.head.id //saved first shown last

      comments foreach deleteComment

      val emptyComments = commentService.getCommentsByPostId(postId)

      emptyComments.isEmpty shouldEqual true
    }

    it("correctly create comment from Json") {
      val postId = "test_post_id"

      val comment = DomainEntityGenerator.createCommentWithoutId(postId)
      val  result = commentService.createComment(Json.toJson(comment))

      result.isRight shouldEqual true
      val id = result.right.get
      keys.enqueue(id)

      val foundComment = getById(id)

      foundComment.isDefined shouldEqual true
      foundComment.get.id shouldEqual Some(id)
      foundComment.get.postId shouldEqual postId

      commentService.deleteComment(id)

      getById(id) shouldEqual None
    }
    it("correctly update review from Json") {
      val postId = "test_post_id"

      val comment = DomainEntityGenerator.createCommentWithoutId(postId = postId)
      val  result = commentService.createComment(Json.toJson(comment))

      result.isRight shouldEqual true
      val id = result.right.get
      keys.enqueue(id)

      val foundComment = getById(id)

      foundComment.isDefined shouldEqual true
      foundComment.get.id shouldEqual Some(id)

      val newText = "some brand new comment body"

      commentService.updateComment(commentId = id,json = Json.toJson(foundComment.get.copy(body = newText)))

      val updatedComment = getById(id)
      updatedComment.isDefined shouldEqual true
      updatedComment.get.id shouldEqual Some(id)
      updatedComment.get.body shouldEqual newText

      commentService.deleteComment(id)

      getById(id) shouldEqual None
    }
  }

  private def saveComment(comment: Comment) = {
    saveCommentByKey(comment.id.get, comment)
  }

  private def deleteComment(review: Comment) = {
    deleteByKey(review.id.get)
  }

  private def getById(uid: String) = {
    Await.result(commentDao.get(uid), 5 seconds)
  }

  private def saveCommentByKey(key: String, review: Comment) = {
    keys.enqueue(key)
    Await.result(commentDao.save(key, review), 5 seconds)
  }

  private def deleteByKey(key: String) = {
    Await.result(commentDao.delete(key), 5 seconds)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    keys foreach deleteByKey

    context.close()
    context.destroy()
  }
}

