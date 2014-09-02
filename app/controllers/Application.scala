package controllers

import securesocial.core._
import play.api.mvc.{BodyParsers, RequestHeader}
import auth.SocialUser
import play.api.libs.json.{JsValue, JsError, Json}
import domain.{Preview, Post, PostDTO}
import domain.DomainJsonFormats._
import util.{StringAndDateUtils, JsonTransformer}
import dao.{TagDao, PostDAO}
import scala.concurrent.Await
import scala.concurrent.duration._
import play.mvc.Result
import scala.util.Random

class Application(override implicit val env: RuntimeEnvironment[SocialUser]) extends securesocial.core.SecureSocial[SocialUser] {

  def index = UserAwareAction { implicit request =>
    val previews = Await.result(PostDAO.findSubmittedPosts(), 5 seconds) map {post => Preview.fromPost(post)}

    Ok(views.html.index(request.user)(previews))
  }

  def editpost(uid: Option[String]) = UserAwareAction { implicit request =>
    uid map { uid =>
      Await.result(PostDAO.get(uid), 5 seconds) map { post =>

        val tags = if(post.tags.isEmpty) None else Some(Json.toJson(post.tags).toString)

        println(tags)

        Ok(views.html.editpost(post.id, Some(post.title), Some(JsonTransformer.buildSirTrevorBlocks(post).toString()), tags)(request.user))
      } getOrElse(BadRequest(s"Post with uid $uid not found!"))
    } getOrElse Ok(views.html.editpost(None, None, None, None)(request.user))
  }

  def post(uid: String) = UserAwareAction { implicit request =>
    Await.result(PostDAO.get(uid), 5 seconds) map { post =>
      println(s"uid: $uid\npost: $post")
        Ok(views.html.post(post)(request.user))
      } getOrElse(Redirect("/"))
  }

  def submit(uid: String) = UserAwareAction { implicit request =>
    println(s"submit request for post with id: $uid")
    Await.result(PostDAO.get(uid.trim), 5 seconds) map { post =>
      val newPost = post.copy(isDraft = false)
      PostDAO.save(uid, newPost)

      TagDao.mergeTags(post.tags)//! happens only when post submitted to minimize amount of trash tags

      Redirect("/")
    } getOrElse(BadRequest(s"Draft with uid $uid does not exist!"))
  }

  def delete(uid: String) = UserAwareAction { implicit request =>
    println(s"delete request for post with id: $uid")
    PostDAO.delete(uid.trim)
    Ok("post deleted")
  }

  def drafts = UserAwareAction { implicit request =>
    val previews = Await.result(PostDAO.findDrafts(), 5 seconds) map {post => Preview.fromPost(post)}

    Ok(views.html.index(request.user)(previews))
  }

  def uploadPost = UserAwareAction(BodyParsers.parse.json) {
    implicit request =>
      val post = JsonTransformer.createPostFromJson(request.body)

      post map {p =>
        val generatedUID = StringAndDateUtils.generateUID(p.title)

        PostDAO.save(generatedUID, post.get.copy(id = Some(generatedUID), isDraft = true, date = StringAndDateUtils.getCurrentDateAsString))

        Ok(Json.obj("status" -> "OK", "pid" -> generatedUID))
      } getOrElse {
        BadRequest(Json.obj("status" -> "KO", "message" -> "some errors with json structure"))
      }
  }

  def updatePost(uid: String) = UserAwareAction(BodyParsers.parse.json) {
    implicit request =>

      val post = JsonTransformer.createPostFromJson(request.body)

      post map {p =>
        val generatedUID = StringAndDateUtils.generateUID(p.title)

        PostDAO.save(generatedUID, post.get.copy(id = Some(generatedUID), date = StringAndDateUtils.getCurrentDateAsString))
        println(s"in updatePost => uid: $uid\npost: $post")

        if(uid != generatedUID){
            PostDAO.delete(uid)
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