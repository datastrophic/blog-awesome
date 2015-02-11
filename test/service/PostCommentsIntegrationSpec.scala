package service

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, FunSpec}
import scala.collection.mutable
import java.util.Date
import play.api.libs.json.Json
import scala.concurrent.Await
import util.{DomainEntityGenerator, SpringContextHelper}
import dao.{PostDao, CommentDao}
import domain.{Post, Comment}
import scala.concurrent.duration._
import scala.collection.JavaConversions._
import dao.PostDao
import domain.JsonFormats._
import scala.concurrent.ExecutionContext.Implicits.global

@RunWith(classOf[JUnitRunner])
class PostCommentsIntegrationSpec extends FunSpec with Matchers with BeforeAndAfterAll{

  val context = SpringContextHelper.springContext

  private val commentDao: CommentDao = context.getBean(classOf[CommentDao])
  private val commentService: CommentService = context.getBean(classOf[CommentService])
  private val postDao: PostDao = context.getBean(classOf[PostDao])
  private val postService: PostService = context.getBean(classOf[PostService])

  private val keys = mutable.Queue.empty[String]

  describe("Post Service"){

    it("deletes all comment on post deletion") {
      val post = DomainEntityGenerator.createBlankPost
      savePost(post)


      val postId = post.id.get
      val comments = (1 to 10).map(num => DomainEntityGenerator.createComment(s"uid_$num", postId))

      comments foreach { comment =>
        val time = new Date().getTime
        saveComment(comment.copy(timestamp = Some(time)))
        Thread.sleep(5)
      }

      val foundComments = commentService.getCommentsByPostId(postId)

      foundComments.isEmpty shouldEqual false
      foundComments.size shouldEqual 10

      foundComments.head.postId shouldEqual postId
      foundComments.last.postId shouldEqual postId

      postService.deletePostById(postId)

      val emptyComments = commentService.getCommentsByPostId(postId)

      emptyComments.isEmpty shouldEqual true
    }
  }

  private def saveComment(comment: Comment) = {
    val key = comment.id.get
    keys.enqueue(key)
    Await.result(commentDao.save(key, comment), 5 seconds)
  }

  private def deleteByKey(key: String) = {
    Await.result(commentDao.delete(key), 5 seconds)
    Await.result(postDao.delete(key), 5 seconds)
  }

  private def savePost(post: Post) = {
    val key = post.id.get
    keys.enqueue(key)
    Await.result(postDao.save(key, post), 5 seconds)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    keys foreach deleteByKey

    context.close()
    context.destroy()
  }
}

