package controllers

import dao.PostDao
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Controller, Action}

import scala.concurrent.Await
import scala.concurrent.duration._

class SiteMapController (postDao: PostDao) extends Controller{

  private val logger = Logger("[SiteMapController]")

  def getSiteMap = Action {
    implicit request =>
      logger.info(s"Start reading posts ids list from DB")

      val postIds = Await.result(postDao.getSitemapIds, 5 seconds)

      logger.info(s"${postIds.size} ids are read from DB")




      Ok(Json.toJson(postIds))
  }


  private def buildSitemapXML(ids: List[String]): String ={


    ""
  }
}