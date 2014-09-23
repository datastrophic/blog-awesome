package service

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import domain.{DataBlock, PostPreview, ViewPage, Post}
import dao.PostDAO
import scala.concurrent.Await
import scala.concurrent.duration._
import util.{StringAndDateUtils, PostGenerator}
import play.api.libs.json.{Json, JsValue}
import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.JavaConversions._


class PostServiceSpec extends Specification with NoTimeConversions{

  private val keys = new ConcurrentLinkedQueue[String]()

  sequential

  "PostService" should {

    "correctly get post by id [getPostById]" in {

      val sampleUid = "post_key"

      val post = PostGenerator.createBlankPost.copy(id = Some(sampleUid))
      savePost(post)

      val foundPost = PostService.getPostById(sampleUid)

      foundPost.isDefined mustEqual true
      foundPost.get.id mustEqual post.id
      foundPost.get.title mustEqual post.title
      foundPost.get.date mustEqual post.date

      deletePost(post)

      PostService.getPostById(sampleUid).isDefined mustEqual false
    }

    "correctly get posts list with pagination [getPosts]" in {
      val amount = 23
      val posts = PostGenerator.generatePublishedPosts(amount)

      posts foreach savePost

      val firstPagePosts = PostService.getPosts(None)
      firstPagePosts.size mustEqual ViewPage.PageSize
      firstPagePosts.head.id mustEqual posts.last.id.get //saved last shown first

      val lastPagePosts = PostService.getPosts(Some(amount / ViewPage.PageSize + 1))
      lastPagePosts.size mustEqual amount % ViewPage.PageSize
      lastPagePosts.last.id mustEqual posts.head.id.get //saved last shown first

      posts foreach deletePost

      val expectedEmptyList = PostService.getPosts(None)
      expectedEmptyList.size mustEqual 0
    }

    "correctly get drafts with pagination [getDrafts]" in {
      val amount = 23
      val posts = PostGenerator.generateDrafts(amount)

      posts foreach savePost

      val firstPagePosts = PostService.getDrafts(None)
      firstPagePosts.size mustEqual ViewPage.PageSize
      firstPagePosts.head.id mustEqual posts.last.id.get //saved last shown first

      val lastPagePosts = PostService.getDrafts(Some(amount / ViewPage.PageSize + 1))
      lastPagePosts.size mustEqual amount % ViewPage.PageSize
      lastPagePosts.last.id mustEqual posts.head.id.get //saved last shown first

      posts foreach deletePost

      val expectedEmptyList = PostService.getPosts(None)
      expectedEmptyList.size mustEqual 0
    }
    "correctly get posts by tag with pagination [getPostsByTag]" in {
      val tags1 = List("sample_tag", "sample_tag_2")
      val tags2 = List("another tag", "sample_tag")
      val amount = 23

      val postsTag1 = PostGenerator.generateTaggedPosts(tags1, amount)
      val postTag2 = PostGenerator.createPostWithTags(tags2).copy(id = Some("tag2_test_post"))

      postsTag1 foreach savePost
      savePost(postTag2)

      val firstPagePosts = PostService.getPostsByTag(tags1.head, None)
      firstPagePosts.size mustEqual ViewPage.PageSize
      firstPagePosts.head.id mustEqual postTag2.id.get //saved last shown first
      firstPagePosts.head.tags.size mustEqual tags1.size
      firstPagePosts.head.tags.contains(tags1.head) mustEqual true
      firstPagePosts.head.tags.contains(tags1.last) mustEqual false
      firstPagePosts.head.tags.contains(tags2.last) mustEqual true

      val lastPagePosts = PostService.getPostsByTag(tags1.head, Some(amount / ViewPage.PageSize + 1))
      lastPagePosts.size mustEqual amount % ViewPage.PageSize+1
      lastPagePosts.last.id mustEqual postsTag1.head.id.get //saved last shown first
      lastPagePosts.head.tags.size mustEqual tags1.size
      lastPagePosts.head.tags.contains(tags1.head) mustEqual true
      lastPagePosts.head.tags.contains(tags1.last) mustEqual true

      val singleTagPosts = PostService.getPostsByTag(tags2.head, None)
      singleTagPosts.size mustEqual 1
      singleTagPosts.head.id mustEqual postTag2.id.get
      singleTagPosts.head.tags.size mustEqual tags2.size
      singleTagPosts.head.tags.contains(tags2.head) mustEqual true
      singleTagPosts.head.tags.contains(tags2.last) mustEqual true

      postsTag1 foreach deletePost
      deletePost(postTag2)

      val expectedEmptyList = PostService.getPostsByTag(tags1.head, None)
      expectedEmptyList.size mustEqual 0
    }

    "correctly delete post by id [deletePostById]" in {

      val uid = "Sample uid"
      val post = PostGenerator.createBlankPost.copy(id = Some(uid))
      savePost(post)

      val foundPost = getById(uid)
      foundPost.isDefined mustEqual true
      foundPost.get.id mustEqual post.id

      PostService.deletePostById(uid)

      getById(uid).isDefined mustEqual false
    }

    "correctly save post from JSON [saveJsonPost]" in {

      val title = "test title"

      val sample =
        s"""
          |{"title":"$title","data":[
          |{"type":"text","data":{"text":"test text"}},
          |{"type":"image","data":{"file":{"url":"http://localhost:9000/images/kbr9i9970sq44m04gscu8p7v50.jpg"}}},
          |{"type":"gist","data":{"id":"00c4d337dbc5c9ea4cbc"}}
          |],"tags":["tag1","tag2","tag3"]}
        """.stripMargin

      val json: JsValue = Json.parse(sample)

      val result = PostService.saveJsonPost(json)
      result.isRight mustEqual true

      val generatedUid = StringAndDateUtils.generateUID(title)

      result.right.get mustEqual generatedUid

      val expectedPost = getById(generatedUid)

      expectedPost.isDefined mustEqual true
      deletePost(expectedPost.get)

      expectedPost.get.title mustEqual title
      expectedPost.get.body.size mustEqual 3
      expectedPost.get.tags.size mustEqual 3

    }

    "correctly update existing post with uid rewrite[updateExistingPost]" in {
      val title = "test title"
      val generatedUid = StringAndDateUtils.generateUID(title)

      val samplePost = PostGenerator.createBlankPost.copy(id = Some(generatedUid))

      savePost(samplePost)


      getById(generatedUid).isDefined mustEqual true

      val newTitle = "some other title"
      val newGeneratedUid = StringAndDateUtils.generateUID(newTitle)

      val updateSample =
        s"""
          |{"title":"$newTitle","data":[
          |{"type":"text","data":{"text":"test text"}},
          |{"type":"image","data":{"file":{"url":"http://localhost:9000/images/kbr9i9970sq44m04gscu8p7v50.jpg"}}},
          |{"type":"gist","data":{"id":"00c4d337dbc5c9ea4cbc"}}
          |],"tags":["tag1","tag2","tag3"]}
        """.stripMargin

      val json: JsValue = Json.parse(updateSample)


      val result = PostService.updateExistingPost(generatedUid, json)
      result.isRight mustEqual true
      result.right.get mustEqual newGeneratedUid

      //post with ol title as key must be deleted
      getById(generatedUid).isDefined mustEqual false

      val updatedPost = getById(newGeneratedUid)

      updatedPost.isDefined mustEqual true
      deletePost(updatedPost.get)

      updatedPost.get.title mustEqual newTitle
      updatedPost.get.body.size mustEqual 3
      updatedPost.get.tags.size mustEqual 3
    }

    "correctly publish draft post [publishPost]" in {

      val uid = "test_uid"
      val draft = PostGenerator.createDraftPost.copy(id = Some(uid))

      savePost(draft)

      val result = PostService.publishPost(uid)
      result.isRight mustEqual true
      result.right.get mustEqual true

      val publishedPost = getById(uid)
      publishedPost.isDefined mustEqual true
      publishedPost.get.id mustEqual draft.id
      publishedPost.get.isDraft mustEqual false
    }

    "correctly create view page [createViewPage]" in {
      val fullPreviews = (1 to ViewPage.PageSize).map(num => PostPreview(num.toString, s"${num}_sample", "date", List(DataBlock("",""),DataBlock("","")))).toList
      val partialPreviews = (1 to ViewPage.PageSize/2).map(num => PostPreview(num.toString, s"${num}_sample", "date", List(DataBlock("",""),DataBlock("","")))).toList

      val sourceUrl = "/"

      //first page
      val firstPageView = PostService.createViewPage(sourceUrl, fullPreviews, None)
      firstPageView.previous.isDefined mustEqual false
      firstPageView.next.isDefined mustEqual true
      firstPageView.next.get mustEqual s"$sourceUrl?page=2"

      //second page
      val secondPageView = PostService.createViewPage(sourceUrl, fullPreviews, Some(2))
      secondPageView.previous.isDefined mustEqual true
      secondPageView.previous.get mustEqual s"$sourceUrl"

      secondPageView.next.isDefined mustEqual true
      secondPageView.next.get mustEqual s"$sourceUrl?page=3"

      //somewhere in the middle
      val middlePageView = PostService.createViewPage(sourceUrl, fullPreviews, Some(4))
      middlePageView.previous.isDefined mustEqual true
      middlePageView.previous.get mustEqual s"$sourceUrl?page=3"

      middlePageView.next.isDefined mustEqual true
      middlePageView.next.get mustEqual s"$sourceUrl?page=5"

      //last page
      val lastPageView = PostService.createViewPage(sourceUrl, partialPreviews, Some(4))
      lastPageView.previous.isDefined mustEqual true
      lastPageView.previous.get mustEqual s"$sourceUrl?page=3"

      lastPageView.next.isDefined mustEqual false

      //nothing to display
      val emptyPageView = PostService.createViewPage(sourceUrl, List(), Some(4))
      emptyPageView.previous.isDefined mustEqual true
      emptyPageView.previous.get mustEqual s"$sourceUrl?page=3"

      emptyPageView.next.isDefined mustEqual false
    }

    //cleanup step
    step(cleanUp)
  }

  private def cleanUp{
    keys foreach deleteByKey
  }

  private def savePost(post: Post) = {
    savePostByKey(post.id.get, post)
  }

  private def deletePost(post: Post) = {
    deleteByKey(post.id.get)
  }

  private def getById(uid: String) = {
    Await.result(PostDAO.get(uid), 5 seconds)
  }

  private def savePostByKey(key: String, post: Post) = {
    keys.add(key)
    Await.result(PostDAO.save(key, post), 5 seconds)
  }

  private def deleteByKey(key: String) = {
    Await.result(PostDAO.delete(key), 5 seconds)
  }
}
