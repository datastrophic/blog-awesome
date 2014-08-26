package controllers

import securesocial.core._
import play.api.mvc.{BodyParsers, RequestHeader}
import auth.SocialUser
import play.api.libs.json.{JsError, Json}
import domain.{Post, PostDTO}
import domain.DomainJsonFormats._
import util.JsonTransformer
import dao.PostDAO
import scala.concurrent.Await
import scala.concurrent.duration._

class Application(override implicit val env: RuntimeEnvironment[SocialUser]) extends securesocial.core.SecureSocial[SocialUser] {

  def index = UserAwareAction { implicit request =>
      Ok(views.html.index("Hey, Tony!")(request.user))
  }

  def editpost(uid: Option[String]) = UserAwareAction { implicit request =>
    uid map { uid =>
      Await.result(PostDAO.get(uid), 5 seconds) map { post =>

        Ok(views.html.editpost(Some(post.title), Some(JsonTransformer.buildSirTrevorBlocks(post).toString()))(request.user))
      } getOrElse(BadRequest(s"Post with uid $uid not found!"))
    } getOrElse Ok(views.html.editpost(None, None)(request.user))
  }

  def post(uid: String) = UserAwareAction { implicit request =>
    Await.result(PostDAO.get(uid), 5 seconds) map { post =>
      println(s"uid: $uid\npost: $post")
        Ok(views.html.post(post)(request.user))
      } getOrElse(Redirect("/"))
  }

  def submit(uid: String) = UserAwareAction { implicit request =>
    println(s"submit request for post with id: $uid")
    Await.result(PostDAO.get(uid), 5 seconds) map { post =>
      val newPost = post.copy(isDraft = false)
      PostDAO.update(newPost)
      Redirect("/")
    } getOrElse(BadRequest(s"Draft with uid $uid does not exist!"))
  }

  def delete(uid: String) = UserAwareAction { implicit request =>
    println(s"delete request for post with id: $uid")
    PostDAO.delete(uid)
    Redirect("/")
  }

  def drafts = TODO

  def uploadPost(oldUid: Option[String]) = UserAwareAction(BodyParsers.parse.json) {
    implicit request =>
//      println(request.body)
//      Post(id: String, title: String, preview: String, body: String, date: String, tags: List[String] = List(), comments: List[Comment] = List())
      //TODO: check UID not exist [if exist add date to the end]
      val post = JsonTransformer.createPostFromJson(request.body)

      post map {p =>
        //TODO: generated uid: DD-MM-YYYY-post-title-name
        val generatedUID = System.currentTimeMillis().toString

        PostDAO.save(generatedUID, post.get.copy(id = Some(generatedUID)))

        p.id map {uid =>
          if(uid != generatedUID){
            PostDAO.delete(uid)
          }
        }
        Ok(Json.obj("status" -> "OK", "pid" -> generatedUID))
      } getOrElse {
        BadRequest(Json.obj("status" -> "KO", "message" -> "some errors with json structure"))
      }
  }

}

// An Authorization implementation that only authorizes uses that logged in using twitter
case class WithProvider(provider: String) extends Authorization[SocialUser] {
  def isAuthorized(user: SocialUser, request: RequestHeader) = {
    user.profile.providerId == provider
  }
}