package service

import domain.{DataBlock, PostPreview, ViewPage, Post}
import scala.concurrent.Await
import scala.concurrent.duration._
import util.{SpringContextHelper, StringAndDateUtils, DomainEntityGenerator}
import play.api.libs.json.{Json, JsValue}
import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.JavaConversions._
import dao.PostDao
import domain.JsonFormats._
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, Matchers, FunSpec}
import scala.concurrent.ExecutionContext.Implicits.global


class PostServiceIntegrationSpec extends FunSpec with Matchers with BeforeAndAfterAll{

  private val keys = new ConcurrentLinkedQueue[String]()

  val context = SpringContextHelper.springContext

  private val postDao: PostDao = context.getBean(classOf[PostDao])
  private val postService: PostService = context.getBean(classOf[PostService])

  describe("Post Service"){

    it("correctly get post by id [getPostById]"){

      val sampleUid = "post_key"

      val post = DomainEntityGenerator.createBlankPost.copy(id = Some(sampleUid))
      savePost(post)

      val foundPost = postService.getPostById(sampleUid)

      foundPost.isDefined shouldEqual true
      foundPost.get.id shouldEqual post.id
      foundPost.get.title shouldEqual post.title
      foundPost.get.date shouldEqual post.date

      deletePost(post)

      postService.getPostById(sampleUid).isDefined shouldEqual false
    }

    it("correctly get posts list with pagination [getPosts]"){
      val amount = 23
      val posts = DomainEntityGenerator.generatePublishedPosts(amount)

      posts foreach savePost

      val firstPagePosts = postService.getPosts(None)
      firstPagePosts.size shouldEqual ViewPage.PageSize
      firstPagePosts.head.id shouldEqual posts.last.id.get //saved last shown first

      val lastPagePosts = postService.getPosts(Some(amount / ViewPage.PageSize + 1))
      lastPagePosts.size shouldEqual amount % ViewPage.PageSize
      lastPagePosts.last.id shouldEqual posts.head.id.get //saved last shown first

      posts foreach deletePost

      val expectedEmptyList = postService.getPosts(None)
      expectedEmptyList.size shouldEqual 0
    }

    it("correctly get drafts with pagination [getDrafts]"){
      val amount = 23
      val posts = DomainEntityGenerator.generateDrafts(amount)

      posts foreach savePost

      val firstPagePosts = postService.getDrafts(None)
      firstPagePosts.size shouldEqual ViewPage.PageSize
      firstPagePosts.head.id shouldEqual posts.last.id.get //saved last shown first

      val lastPagePosts = postService.getDrafts(Some(amount / ViewPage.PageSize + 1))
      lastPagePosts.size shouldEqual amount % ViewPage.PageSize
      lastPagePosts.last.id shouldEqual posts.head.id.get //saved last shown first

      posts foreach deletePost

      val expectedEmptyList = postService.getPosts(None)
      expectedEmptyList.size shouldEqual 0
    }
    it("correctly get posts by tag with pagination [getPostsByTag]"){
      val tags1 = List("sample_tag", "sample_tag_2")
      val tags2 = List("another tag", "sample_tag")
      val amount = 23

      val postsTag1 = DomainEntityGenerator.generateTaggedPosts(tags1, amount)
      val postTag2 = DomainEntityGenerator.createPostWithTags(tags2).copy(id = Some("tag2_test_post"))

      postsTag1 foreach savePost
      savePost(postTag2)

      val firstPagePosts = postService.getPostsByTag(tags1.head, None)
      firstPagePosts.size shouldEqual ViewPage.PageSize
      firstPagePosts.head.id shouldEqual postTag2.id.get //saved last shown first
      firstPagePosts.head.tags.size shouldEqual tags1.size
      firstPagePosts.head.tags.contains(tags1.head) shouldEqual true
      firstPagePosts.head.tags.contains(tags1.last) shouldEqual false
      firstPagePosts.head.tags.contains(tags2.last) shouldEqual true

      val lastPagePosts = postService.getPostsByTag(tags1.head, Some(amount / ViewPage.PageSize + 1))
      lastPagePosts.size shouldEqual amount % ViewPage.PageSize+1
      lastPagePosts.last.id shouldEqual postsTag1.head.id.get //saved last shown first
      lastPagePosts.head.tags.size shouldEqual tags1.size
      lastPagePosts.head.tags.contains(tags1.head) shouldEqual true
      lastPagePosts.head.tags.contains(tags1.last) shouldEqual true

      val singleTagPosts = postService.getPostsByTag(tags2.head, None)
      singleTagPosts.size shouldEqual 1
      singleTagPosts.head.id shouldEqual postTag2.id.get
      singleTagPosts.head.tags.size shouldEqual tags2.size
      singleTagPosts.head.tags.contains(tags2.head) shouldEqual true
      singleTagPosts.head.tags.contains(tags2.last) shouldEqual true

      postsTag1 foreach deletePost
      deletePost(postTag2)

      val expectedEmptyList = postService.getPostsByTag(tags1.head, None)
      expectedEmptyList.size shouldEqual 0
    }

    it("correctly delete post by id [deletePostById]"){

      val uid = "Sample uid"
      val post = DomainEntityGenerator.createBlankPost.copy(id = Some(uid))
      savePost(post)

      val foundPost = getById(uid)
      foundPost.isDefined shouldEqual true
      foundPost.get.id shouldEqual post.id

      postService.deletePostById(uid)

      getById(uid).isDefined shouldEqual false
    }

    it("correctly save post from JSON [saveJsonPost]"){

      val title = "test title"

      //Strings are used for testing because SirTrevor stores data in its own json format
      //which is passed from frontend and then transformed into domain object
      val sample =
        s"""
          |{"title":"$title","data":[
          |{"type":"text","data":{"text":"test text"}},
          |{"type":"image","data":{"file":{"url":"http://localhost:9000/images/kbr9i9970sq44m04gscu8p7v50.jpg"}}},
          |{"type":"gist","data":{"id":"00c4d337dbc5c9ea4cbc"}}
          |],"tags":["tag1","tag2","tag3"]}
        """.stripMargin

      //todo: use json conversions, not plain text

      val json: JsValue = Json.parse(sample)

      val result = postService.saveJsonPost(json)
      result.isRight shouldEqual true

      val generatedUid = StringAndDateUtils.generateUID(title)

      result.right.get shouldEqual generatedUid

      val expectedPost = getById(generatedUid)

      expectedPost.isDefined shouldEqual true
      deletePost(expectedPost.get)

      expectedPost.get.title shouldEqual title
      expectedPost.get.body.size shouldEqual 3
      expectedPost.get.tags.size shouldEqual 3

    }

    it("correctly update existing post with uid rewrite[updateExistingPost]"){
      val title = "test title"
      val generatedUid = StringAndDateUtils.generateUID(title)

      val samplePost = DomainEntityGenerator.createBlankPost.copy(id = Some(generatedUid))

      savePost(samplePost)

      getById(generatedUid).isDefined shouldEqual true

      val newTitle = "some other title"
      val newGeneratedUid = StringAndDateUtils.generateUID(newTitle)

      //Strings are used for testing because SirTrevor stores data in its own json format
      //which is passed from frontend and then transformed into domain object
      val updateSample =
        s"""
          |{"title":"$newTitle","data":[
          |{"type":"text","data":{"text":"test text"}},
          |{"type":"image","data":{"file":{"url":"http://localhost:9000/images/kbr9i9970sq44m04gscu8p7v50.jpg"}}},
          |{"type":"gist","data":{"id":"00c4d337dbc5c9ea4cbc"}}
          |],"tags":["tag1","tag2","tag3"]}
        """.stripMargin

      val json: JsValue = Json.parse(updateSample)


      val result = postService.updateExistingPost(generatedUid, json)
      result.isRight shouldEqual true
      result.right.get shouldEqual newGeneratedUid

      //post with ol title as key must be deleted
      getById(generatedUid).isDefined shouldEqual false

      val updatedPost = getById(newGeneratedUid)

      updatedPost.isDefined shouldEqual true
      deletePost(updatedPost.get)

      updatedPost.get.title shouldEqual newTitle
      updatedPost.get.body.size shouldEqual 3
      updatedPost.get.tags.size shouldEqual 3
    }

    it("correctly publish draft post [publishPost]"){

      val uid = "test_uid"
      val draft = DomainEntityGenerator.createDraftPost.copy(id = Some(uid))

      savePost(draft)

      val result = postService.publishPost(uid)
      result.isRight shouldEqual true
      result.right.get shouldEqual true

      val publishedPost = getById(uid)
      publishedPost.isDefined shouldEqual true
      publishedPost.get.id shouldEqual draft.id
      publishedPost.get.isDraft shouldEqual false
    }

    it("correctly create view page [createViewPage]"){
      val fullPreviews = (1 to ViewPage.PageSize).map(num => PostPreview(num.toString, s"${num}_sample", "date", List(DataBlock("",""),DataBlock("","")))).toList
      val partialPreviews = (1 to ViewPage.PageSize/2).map(num => PostPreview(num.toString, s"${num}_sample", "date", List(DataBlock("",""),DataBlock("","")))).toList

      val sourceUrl = "/"

      //first page
      val firstPageView = postService.createViewPage(sourceUrl, fullPreviews, None)
      firstPageView.previous.isDefined shouldEqual false
      firstPageView.next.isDefined shouldEqual true
      firstPageView.next.get shouldEqual s"$sourceUrl?page=2"

      //second page
      val secondPageView = postService.createViewPage(sourceUrl, fullPreviews, Some(2))
      secondPageView.previous.isDefined shouldEqual true
      secondPageView.previous.get shouldEqual s"$sourceUrl"

      secondPageView.next.isDefined shouldEqual true
      secondPageView.next.get shouldEqual s"$sourceUrl?page=3"

      //somewhere in the middle
      val middlePageView = postService.createViewPage(sourceUrl, fullPreviews, Some(4))
      middlePageView.previous.isDefined shouldEqual true
      middlePageView.previous.get shouldEqual s"$sourceUrl?page=3"

      middlePageView.next.isDefined shouldEqual true
      middlePageView.next.get shouldEqual s"$sourceUrl?page=5"

      //last page
      val lastPageView = postService.createViewPage(sourceUrl, partialPreviews, Some(4))
      lastPageView.previous.isDefined shouldEqual true
      lastPageView.previous.get shouldEqual s"$sourceUrl?page=3"

      lastPageView.next.isDefined shouldEqual false

      //nothing to display
      val emptyPageView = postService.createViewPage(sourceUrl, List(), Some(4))
      emptyPageView.previous.isDefined shouldEqual true
      emptyPageView.previous.get shouldEqual s"$sourceUrl?page=3"

      emptyPageView.next.isDefined shouldEqual false
    }
  }

  private def savePost(post: Post) = {
    savePostByKey(post.id.get, post)
  }

  private def deletePost(post: Post) = {
    deleteByKey(post.id.get)
  }

  private def getById(uid: String) = {
    Await.result(postDao.get(uid), 5 seconds)
  }

  private def savePostByKey(key: String, post: Post) = {
    keys.add(key)
    Await.result(postDao.save(key, post), 5 seconds)
  }

  private def deleteByKey(key: String) = {
    Await.result(postDao.delete(key), 5 seconds)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()

    keys foreach deleteByKey

    context.close()
    context.destroy()
  }
}
