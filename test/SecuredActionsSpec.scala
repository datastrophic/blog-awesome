import controllers.{ImageController, PostController}
import dao.PostDao
import domain.Post
import play.api.libs.Files.TemporaryFile
import play.api.libs.iteratee.Input
import play.api.libs.json.JsValue
import play.api.mvc.MultipartFormData.{BadPart, MissingFilePart, FilePart}
import play.api.mvc.{MultipartFormData, Request, Result, Results}
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import util.{SpringContextHelper, JsonPostTransformer, DomainEntityGenerator}
import scala.concurrent.ExecutionContext.Implicits.global
import domain.JsonFormats._

class SecuredActionsSpec extends PlaySpec with Results with OneAppPerSuite{

  private def errorMessage = "{\"error\":\"Credentials required\"}"

  val context = SpringContextHelper.springContext
  private val postDao: PostDao = context.getBean(classOf[PostDao])

  "Secured Actions" should {
    "allow access to index page for unauthorized users" in {
      val controller = Global.getControllerInstance(classOf[PostController])
      val result = controller.index(None).apply(FakeRequest())

      val bodyText: String = contentAsString(result)
      bodyText.contains("<title>Flying Octopus</title>") mustBe true

      bodyText.contains("New Post") mustBe false
      bodyText.contains("Drafts") mustBe false
    }
    "allow access to post page for unauthorized users" in {
      val uid = "controller_test_uid"
      val postTitle = "Sample Post Title for controller test"
      val post = DomainEntityGenerator.createPublishedPost.copy(id = Some(uid), title = postTitle)

      savePostByKey(uid, post)

      val controller = Global.getControllerInstance(classOf[PostController])
      val result = controller.postViewPage(uid).apply(FakeRequest())

      val bodyText: String = contentAsString(result)

      deleteByKey(uid)

      bodyText.contains(postTitle) mustBe true

      bodyText.contains("New Post") mustBe false
      bodyText.contains("Drafts") mustBe false
      bodyText.contains("Edit") mustBe false
    }
    //POST          /post/:uid            @controllers.Application.publishPost(uid)
    "restrict access to post publishing [publishPost(uid)]" in {
      val controller = Global.getControllerInstance(classOf[PostController])
      val result = controller.publishPost("fake_uid").apply(FakeRequest())

      val bodyText: String = contentAsString(result)
      bodyText mustBe errorMessage
    }

    //    DELETE        /post/:uid            @controllers.Application.
    "restrict access to post deletion [delete(uid)]" in {
      val controller = Global.getControllerInstance(classOf[PostController])
      val result = controller.deletePost("fake_uid").apply(FakeRequest())

      val bodyText: String = contentAsString(result)
      bodyText mustBe errorMessage
    }

    //    GET           /edit                 @controllers.Application.editpost(uid: Option[String])
    "restrict access to post edit view [editpost(uid: Option[String])]" in {
      val controller = Global.getControllerInstance(classOf[PostController])
      val createPostPage = controller.postEditPage(None).apply(FakeRequest())

      var bodyText: String = contentAsString(createPostPage)
      bodyText mustBe errorMessage

      val editPostPage = controller.postEditPage(None).apply(FakeRequest())

      bodyText = contentAsString(editPostPage)
      bodyText mustBe errorMessage
    }
    //    POST          /post                 @controllers.Application.createPost
    "restrict access to post creation [createPost]" in {
      val controller = Global.getControllerInstance(classOf[PostController])

      val postJson = JsonPostTransformer.buildSirTrevorBlocks(DomainEntityGenerator.createBlankPost)
      val jsonPostRequest: Request[JsValue] = FakeRequest("POST", "/post").withBody(postJson).withHeaders(("Content-Type", "application/json"))

      val result: Future[Result] = controller.createPost.apply(jsonPostRequest)

      val bodyText = contentAsString(result)
      bodyText mustBe errorMessage
    }
    //    PATCH         /post/:uid            @controllers.Application.updatePost(uid)
    "restrict access to post updating [updatePost(uid)]" in {
      val controller = Global.getControllerInstance(classOf[PostController])
      val postJson = JsonPostTransformer.buildSirTrevorBlocks(DomainEntityGenerator.createBlankPost)
      val jsonPostRequest: Request[JsValue] = FakeRequest("POST", "/post").withBody(postJson).withHeaders(("Content-Type", "application/json"))

      val result: Future[Result] = controller.updatePost("fake_uid").apply(jsonPostRequest)

      val bodyText = contentAsString(result)
      bodyText mustBe errorMessage
    }
    //    GET           /drafts               @controllers.Application.drafts(page: Option[Int])
    "restrict access to drafts page [drafts(page: Option[Int])]" in {
      val controller = Global.getControllerInstance(classOf[PostController])
      val firstPageResult = controller.getDraftsPage(None).apply(FakeRequest())

      var bodyText: String = contentAsString(firstPageResult)
      bodyText mustBe errorMessage

      val secondPageResult = controller.getDraftsPage(None).apply(FakeRequest())

      bodyText = contentAsString(secondPageResult)
      bodyText mustBe errorMessage
    }
    //    POST          /image                @controllers.ImageController.uploadImage
    "restrict access to image upload [uploadImage]" in {
      val controller: ImageController = Global.getControllerInstance(classOf[ImageController])

      val tempFile = TemporaryFile("do_upload","spec")
      val fileName = "testFile.txt"
      val part = FilePart("key: String", fileName, None, tempFile)
      val files = Seq[FilePart[TemporaryFile]](part)
      val multipartBody = MultipartFormData(Map[String, Seq[String]](), files, Seq[BadPart](), Seq[MissingFilePart]())

      val multipartRequest: Request[MultipartFormData[TemporaryFile]] =
        FakeRequest("POST", "/image").withBody(multipartBody).withHeaders(("Content-Type", "multipart/form-data"))

      val result: Future[Result] = controller.uploadImage.apply(multipartRequest)

      val bodyText = contentAsString(result)
      bodyText mustBe errorMessage
    }
    //    GET           /taglist              @controllers.TagsController.getTagList
    "restrict access to tags list [getTagList]" in {
      val controller = Global.getControllerInstance(classOf[PostController])
      val result = controller.getDraftsPage(None).apply(FakeRequest())

      val bodyText: String = contentAsString(result)
      bodyText mustBe errorMessage
    }

  }

  private def savePostByKey(key: String, post: Post) = {
    Await.result(postDao.save(key, post), 5 seconds)
  }

  private def deleteByKey(key: String) = {
    Await.result(postDao.delete(key), 5 seconds)
  }
}
