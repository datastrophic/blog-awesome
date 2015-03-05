package dao

import db.PostBucketClient
import scala.concurrent.Await
import scala.concurrent.duration._
import util.{SpringContextHelper, DomainEntityGenerator}
import domain.{Post, ViewPage}
import domain.JsonFormats._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterEach, FunSpec, BeforeAndAfterAll, Matchers}
import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class PostDaoSpec extends FunSpec with PostBucketClient with Matchers with BeforeAndAfterAll{

  val tid0 = "test_id_0"
  val tid1 = "test_id_1"
  val tid2 = "test_id_2"

  val context = SpringContextHelper.springContext

  private val postDao: PostDao = context.getBean(classOf[PostDao])
  private val keys = mutable.Queue.empty[String]

  describe("Post DAO") {

    it("properly find posts by tag with ordering by date"){

      val tag1 = "tag_under_test_1"
      val tag2 = "tag_under_test_2"

      val taggedPosts = List(
        ("tag_test_0", DomainEntityGenerator.createPostWithTag(tag1).copy(id = Some(tid0))),
        ("tag_test_1", DomainEntityGenerator.createPostWithTag(tag1).copy(id = Some(tid1))),
        ("tag_test_2", DomainEntityGenerator.createPostWithTag(tag2).copy(id = Some(tid2)))
      )

      taggedPosts.foreach(post => {
        Await.result(postDao.save(post._1, post._2), 5 seconds)
        keys.enqueue(post._1)
      })

      val expectedTag1Entities = Await.result(postDao.findPostsByTag(tag1, 1), 5 seconds)
      expectedTag1Entities.size shouldEqual 2
      expectedTag1Entities.head.id shouldEqual Some(tid1)
      expectedTag1Entities.head.tags.size shouldEqual 1
      expectedTag1Entities.head.tags.head shouldEqual tag1

      expectedTag1Entities.tail.head.id shouldEqual Some(tid0)
      expectedTag1Entities.tail.head.tags.size shouldEqual 1
      expectedTag1Entities.tail.head.tags.head shouldEqual tag1

      val expectedTag2Entities = Await.result(postDao.findPostsByTag(tag2, 1), 5 seconds)
      expectedTag2Entities.size shouldEqual 1
      expectedTag2Entities.head.id shouldEqual Some(tid2)
      expectedTag2Entities.head.tags.size shouldEqual 1
      expectedTag2Entities.head.tags.head shouldEqual tag2

      taggedPosts.foreach(post => postDao.delete(post._1))

      val deletedPostsWithTag1 = Await.result(postDao.findPostsByTag(tag1, 1), 5 seconds)
      deletedPostsWithTag1.size shouldEqual 0

      val deletedPostsWithTag2 = Await.result(postDao.findPostsByTag(tag2, 1), 5 seconds)
      deletedPostsWithTag2.size shouldEqual 0
    }

    it("properly find drafts and published posts"){

      val posts = List(
        ("draft_test_0", DomainEntityGenerator.createDraftPost.copy(id = Some(tid0))),
        ("draft_test_1", DomainEntityGenerator.createDraftPost.copy(id = Some(tid1))),
        ("draft_test_2", DomainEntityGenerator.createPublishedPost.copy(id = Some(tid2)))
      )

      posts.foreach(post => {
        Await.result(postDao.save(post._1, post._2), 5 seconds)
        keys.enqueue(post._1)
      })

      val expectedDraftPosts = Await.result(postDao.findDrafts(1), 5 seconds)
      expectedDraftPosts.size shouldEqual 2
      expectedDraftPosts.head.id shouldEqual Some(tid1) //Saved last appears first: desc ordering by date
      expectedDraftPosts.tail.head.id shouldEqual Some(tid0)

      val expectedPublishedPosts = Await.result(postDao.findSubmittedPosts(1), 5 seconds)
      expectedPublishedPosts.size shouldEqual 1
      expectedPublishedPosts.head.id shouldEqual Some(tid2)

      posts.foreach(post => Await.result(postDao.delete(post._1), 5 seconds))

      val deletedDrafts = Await.result(postDao.findDrafts(1), 5 seconds)
      deletedDrafts.size shouldEqual 0

      val deletedPublishedPosts = Await.result(postDao.findSubmittedPosts(1), 5 seconds)
      deletedPublishedPosts.size shouldEqual 0
    }

    it("provide proper pagination and ordering"){
      //default page size is 10, default post state is draft, ids: from zero to amount-1
      val posts = DomainEntityGenerator.generateDrafts(23)

      posts.foreach(post => {
        val uid = posts.indexOf(post).toString
        Await.result(postDao.save(uid, post), 5 seconds)
        keys.enqueue(uid)
      })

      val expectedFirstPagePosts = Await.result(postDao.findDrafts(1), 5 seconds)

      expectedFirstPagePosts.size shouldEqual ViewPage.PageSize
      expectedFirstPagePosts.head.id shouldEqual posts.last.id //saved last, shown first

      val expectedLastPagePosts = Await.result(postDao.findDrafts(3), 5 seconds)

      expectedLastPagePosts.size shouldEqual (posts.size - 2 * ViewPage.PageSize)
      expectedLastPagePosts.last.id shouldEqual posts.head.id

      posts.foreach(post => Await.result(postDao.delete(posts.indexOf(post).toString), 5 seconds))

      var foundPosts: List[Post] = Nil

      posts.foreach { post =>
        Await.result(postDao.get(post.id.get), 5 seconds) match {
          case Some(p) => foundPosts = p :: foundPosts
          case _ =>
        }
      }

      foundPosts.size shouldEqual 0
    }
    it("properly get published post ids for sitemap"){
      val tid0 = "sitemap_0"
      val tid1 = "sitemap_1"
      val tid2 = "sitemap_2"

      val posts = List(
        (tid0, DomainEntityGenerator.createDraftPost.copy(id = Some(tid0))),
        (tid1, DomainEntityGenerator.createPublishedPost.copy(id = Some(tid1))),
        (tid2, DomainEntityGenerator.createPublishedPost.copy(id = Some(tid2)))
      )

      posts.foreach(post => {
        Await.result(postDao.save(post._1, post._2), 5 seconds)
        keys.enqueue(post._1)
      })

      val expectedIds = Await.result(postDao.getSitemapIds, 5 seconds)

      expectedIds.size shouldEqual 2
      expectedIds.contains(tid0)  shouldEqual false
      expectedIds.contains(tid1)  shouldEqual true
      expectedIds.contains(tid2)  shouldEqual true

      posts.foreach(post => Await.result(postDao.delete(post._1), 5 seconds))

      val deletedDrafts = Await.result(postDao.findDrafts(1), 5 seconds)
      deletedDrafts.size shouldEqual 0

      val deletedPublishedPosts = Await.result(postDao.findSubmittedPosts(1), 5 seconds)
      deletedPublishedPosts.size shouldEqual 0
    }
  }

  override protected def afterAll(): Unit = {
    super.afterAll()

    keys foreach { key =>
      executeWithBucket(bucket => bucket.delete(key))
    }

    context.close()
    context.destroy()
  }
}
