//import controllers.Application
//import org.junit.runner.RunWith
//import org.specs2.matcher.ShouldMatchers
//import org.specs2.runner.JUnitRunner
//import play.api.http.HeaderNames
//import play.api.mvc.{Request, AnyContent}
//import play.api.test.{PlaySpecification, FakeApplication, FakeRequest}
//import securesocial.testkit.WithLoggedUser
///**
// * Add your spec here.
// * You can mock out a whole application including requests, plugins etc.
// * For more information, consult the wiki.
// */
//class ApplicationSpec extends PlaySpecification with ShouldMatchers {
//  import WithLoggedUser._
//  def minimalApp = FakeApplication(withoutPlugins=excludedPlugins,additionalPlugins = includedPlugins)
//  "Access secured index " in new WithLoggedUser(minimalApp) {
//
//    val req: Request[AnyContent] = FakeRequest().
//      withHeaders((HeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")).
//      withCookies(cookie) // Fake cookie from the WithloggedUser trait
//
//    val result = controllers.Application.index(req)
//
//    val actual: Int= status(result)
//    actual must be equalTo OK
//  }
//}

//TODO: any user can view the index (1), view separate post (2), posts by tag (3)

/*
# Home page
GET           /                     @controllers.Application.index(page: Option[Int])
GET           /post/tag/:tag        @controllers.Application.postsByTag(tag, page: Option[Int])
GET           /post/:uid            @controllers.Application.post(uid)
POST          /post/:uid            @controllers.Application.publishPost(uid)
DELETE        /post/:uid            @controllers.Application.delete(uid)
GET           /edit                 @controllers.Application.editpost(uid: Option[String])
POST          /post                 @controllers.Application.createPost
PATCH         /post/:uid            @controllers.Application.updatePost(uid)
GET           /drafts               @controllers.Application.drafts(page: Option[Int])
GET           /logout               @controllers.CustomLoginController.logout

POST          /image                @controllers.ImageController.uploadImage
GET           /taglist              @controllers.TagsController.getTagList

->            /auth                 securesocial.Routes
*/
