package controllers

import securesocial.core._
import play.api.mvc.{BodyParsers, RequestHeader}
import auth.SocialUser
import play.api.libs.json.{JsError, Json}
import domain.PostDTO
import domain.DomainJsonFormats._

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
      val placeResult = request.body.validate[PostDTO]
      placeResult.fold(
        errors => {
          println(s"BAD:\n${request.body}\nerrors:$errors")
          BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
        },
        postDTO => {
          println(s"GOOD: $postDTO")

          //TODO: save to DB and redirect

          Redirect(routes.Application.posts("11111"))
        }
      )
      //TODO: onSuccess Redirect to draft preview
  }

}

// An Authorization implementation that only authorizes uses that logged in using twitter
case class WithProvider(provider: String) extends Authorization[SocialUser] {
  def isAuthorized(user: SocialUser, request: RequestHeader) = {
    user.profile.providerId == provider
  }
}