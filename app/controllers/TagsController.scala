package controllers

import securesocial.core.{Authorization, RuntimeEnvironment}
import auth.{SecureSocialAuth, SocialUser}
import play.api.libs.json.Json
import play.api.{Play, Logger}
import dao.TagDao
import scala.concurrent.Await
import scala.concurrent.duration._

class TagsController (override implicit val env: RuntimeEnvironment[SocialUser])
  extends securesocial.core.SecureSocial[SocialUser]  with SecureSocialAuth{

  private val logger = Logger("[TagsController]")

  def getTagList = SecuredAction {
    implicit request =>
      val tags = Await.result(TagDao.getTags, 5 seconds)

      Ok(Json.toJson(tags))
  }
}