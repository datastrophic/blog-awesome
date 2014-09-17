import controllers.PostController
import play.api.mvc.Results
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._

class SecuredActionsSpec extends PlaySpec with Results with OneAppPerSuite{

  "Example Page#index" should {
    "should be valid" in {
      val controller = Global.getControllerInstance(classOf[PostController])
      val result = controller.index(None).apply(FakeRequest())

      val bodyText: String = contentAsString(result)
      bodyText.contains("<title>Flying Octopus</title>") mustBe true
    }
  }
}


  /*
//TODO: make sure that we got either access denied or redirect to home
  private

  GET           /logout               @controllers.CustomLoginController.logout
  POST          /post/:uid            @controllers.Application.publishPost(uid)
  DELETE        /post/:uid            @controllers.Application.delete(uid)
  GET           /edit                 @controllers.Application.editpost(uid: Option[String])
  POST          /post                 @controllers.Application.createPost
  PATCH         /post/:uid            @controllers.Application.updatePost(uid)
  GET           /drafts               @controllers.Application.drafts(page: Option[Int])
  POST          /image                @controllers.ImageController.uploadImage
  GET           /taglist              @controllers.TagsController.getTagList
  */

