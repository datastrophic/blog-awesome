package dao

import org.specs2.mutable._
import db.ReactiveCouchbaseClient
import scala.concurrent.Await
import scala.concurrent.duration._
import org.specs2.time.NoTimeConversions
import util.PostGenerator
import domain.{Post, ViewPage}


class PostDAOSpec extends Specification with ReactiveCouchbaseClient with NoTimeConversions{

  val tid0 = "test_id_0"
  val tid1 = "test_id_1"
  val tid2 = "test_id_2"

  sequential

  "PostDAO" should {

    "properly find posts by tag with ordering by date" in {

      val tag1 = "tag_under_test_1"
      val tag2 = "tag_under_test_2"

      val taggedPosts = List(
        ("tag_test_0", PostGenerator.createPostWithTag(tag1).copy(id = Some(tid0))),
        ("tag_test_1", PostGenerator.createPostWithTag(tag1).copy(id = Some(tid1))),
        ("tag_test_2", PostGenerator.createPostWithTag(tag2).copy(id = Some(tid2)))
      )

      taggedPosts.foreach(post => Await.result(PostDAO.save(post._1, post._2), 5 seconds))

      val expectedTag1Entities = Await.result(PostDAO.findPostsByTag(tag1, 1), 5 seconds)
      expectedTag1Entities.size mustEqual 2
      expectedTag1Entities.head.id mustEqual Some(tid1)
      expectedTag1Entities.head.tags.size mustEqual 1
      expectedTag1Entities.head.tags.head mustEqual tag1

      expectedTag1Entities.tail.head.id mustEqual Some(tid0)
      expectedTag1Entities.tail.head.tags.size mustEqual 1
      expectedTag1Entities.tail.head.tags.head mustEqual tag1

      val expectedTag2Entities = Await.result(PostDAO.findPostsByTag(tag2, 1), 5 seconds)
      expectedTag2Entities.size mustEqual 1
      expectedTag2Entities.head.id mustEqual Some(tid2)
      expectedTag2Entities.head.tags.size mustEqual 1
      expectedTag2Entities.head.tags.head mustEqual tag2

      taggedPosts.foreach(post => PostDAO.delete(post._1))

      val deletedPostsWithTag1 = Await.result(PostDAO.findPostsByTag(tag1, 1), 5 seconds)
      deletedPostsWithTag1.size mustEqual 0

      val deletedPostsWithTag2 = Await.result(PostDAO.findPostsByTag(tag2, 1), 5 seconds)
      deletedPostsWithTag2.size mustEqual 0
    }

    "properly find drafts and published posts" in {

      val posts = List(
        ("draft_test_0", PostGenerator.createDraftPost.copy(id = Some(tid0))),
        ("draft_test_1", PostGenerator.createDraftPost.copy(id = Some(tid1))),
        ("draft_test_2", PostGenerator.createPublishedPost.copy(id = Some(tid2)))
      )

      posts.foreach(post => Await.result(PostDAO.save(post._1, post._2), 5 seconds))

      val expectedDraftPosts = Await.result(PostDAO.findDrafts(1), 5 seconds)
      expectedDraftPosts.size mustEqual 2
      expectedDraftPosts.head.id mustEqual Some(tid1)      //Saved last appears first: desc ordering by date
      expectedDraftPosts.tail.head.id mustEqual Some(tid0)

      val expectedPublishedPosts = Await.result(PostDAO.findSubmittedPosts(1), 5 seconds)
      expectedPublishedPosts.size mustEqual 1
      expectedPublishedPosts.head.id mustEqual Some(tid2)

      posts.foreach(post => Await.result(PostDAO.delete(post._1), 5 seconds))

      val deletedDrafts = Await.result(PostDAO.findDrafts(1), 5 seconds)
      deletedDrafts.size mustEqual 0

      val deletedPublishedPosts = Await.result(PostDAO.findSubmittedPosts(1), 5 seconds)
      deletedPublishedPosts.size mustEqual 0
    }

    "provide proper pagination and ordering" in {
      //default page size is 10, default post state is draft, ids: from zero to amount-1
      val posts = PostGenerator.generate(23)

      posts.foreach(post => Await.result(PostDAO.save(posts.indexOf(post).toString, post), 5 seconds))

      val expectedFirstPagePosts = Await.result(PostDAO.findDrafts(1), 5 seconds)

      expectedFirstPagePosts.size mustEqual ViewPage.PageSize
      expectedFirstPagePosts.head.id mustEqual posts.last.id //saved last, shown first

      val expectedLastPagePosts = Await.result(PostDAO.findDrafts(3), 5 seconds)

      expectedLastPagePosts.size mustEqual (posts.size - 2*ViewPage.PageSize)
      expectedLastPagePosts.last.id mustEqual posts.head.id

      posts.foreach(post => Await.result(PostDAO.delete(posts.indexOf(post).toString), 5 seconds))

      var foundPosts: List[Post] = Nil

      posts.foreach { post =>
        Await.result(PostDAO.get(post.id.get), 5 seconds) match {
          case Some(p) => foundPosts = p :: foundPosts
          case _ =>
        }
      }

      foundPosts.size mustEqual 0
    }
  }
}
