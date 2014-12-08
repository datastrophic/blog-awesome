package controllers

import securesocial.core.{Authorization, RuntimeEnvironment}
import auth.{SecureSocialAuth, SocialUser}
import play.api.libs.json.Json
import play.api.{Play, Logger}
import dao.TagDao
import scala.concurrent.Await
import scala.concurrent.duration._

class TagController (tagDao: TagDao, override implicit val env: RuntimeEnvironment[SocialUser])
  extends securesocial.core.SecureSocial[SocialUser]  with SecureSocialAuth{

  private val logger = Logger("[TagsController]")

  def getTagList = SecuredAction {
    implicit request =>
      logger.info(s"Start reading tag list from DB")

      val tags = Await.result(tagDao.getTags, 5 seconds)

      logger.info(s"${tags.size} tags are read from DB")

      Ok(Json.toJson(tags))
  }
}