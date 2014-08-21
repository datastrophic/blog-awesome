package controllers

import securesocial.core._
import play.api.mvc.{BodyParsers, RequestHeader}
import auth.SocialUser
import play.api.libs.json.{JsError, Json}
import domain.{Post, PostDTO}
import domain.DomainJsonFormats._
import util.JsonTransformer

class Application(override implicit val env: RuntimeEnvironment[SocialUser]) extends securesocial.core.SecureSocial[SocialUser] {

  def index = UserAwareAction { implicit request =>
      Ok(views.html.index("Hey, Tony!")(request.user))
  }

  def editpost(postName: Option[String]) = UserAwareAction { implicit request =>
    //TODO: add post contents in case of EDIT action
    Ok(views.html.editpost("Hey, Tony!")(request.user))
  }

  def posts(postName: String) = UserAwareAction { implicit request =>
    Ok(views.html.post("Hey, Tony!")(request.user))
  }

  def uploadPost = UserAwareAction(BodyParsers.parse.json) {
    implicit request =>
      println(request.body)
//      Post(id: String, title: String, preview: String, body: String, date: String, tags: List[String] = List(), comments: List[Comment] = List())

      val post = JsonTransformer.createPostFromJson(request.body)

      if(post.isDefined){
        //TODO: save post, get ID and redirect to preview
        println(s"GOOD: ${post.get}")
        Redirect(routes.Application.posts("11111"))
      } else {
        println(s"BAD")
        BadRequest(Json.obj("status" -> "KO", "message" -> "some errors with json structure"))
      }


      //TODO: onSuccess Redirect to draft preview
  }

}

// An Authorization implementation that only authorizes uses that logged in using twitter
case class WithProvider(provider: String) extends Authorization[SocialUser] {
  def isAuthorized(user: SocialUser, request: RequestHeader) = {
    user.profile.providerId == provider
  }
}