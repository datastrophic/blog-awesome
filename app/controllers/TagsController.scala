package controllers

import securesocial.core.{Authorization, RuntimeEnvironment}
import auth.SocialUser
import play.api.mvc.{BodyParsers, RequestHeader}
import play.api.libs.json.Json
import play.api.{Play, Logger}
import dao.TagDao
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by akirillov on 8/20/14.
 */
class TagsController (override implicit val env: RuntimeEnvironment[SocialUser]) extends securesocial.core.SecureSocial[SocialUser] {
  private val logger = Logger("[TagsController]")



  def getTagList = UserAwareAction {
    implicit request =>
      Await.result(TagDao.getTags, 5 seconds) map {
        tags => Ok(Json.toJson(tags))
      } getOrElse {
        Ok(Json.toJson(List[String]()))
      }
  }

  // An Authorization implementation that only authorizes uses that logged in using twitter
  case class WithProvider(provider: String) extends Authorization[SocialUser] {
    def isAuthorized(user: SocialUser, request: RequestHeader) = {
      user.profile.providerId == provider
    }
  }

}