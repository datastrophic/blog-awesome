package controllers

import com.typesafe.config.ConfigFactory
import dao.PostDao
import play.api.Logger
import play.api.mvc.{Controller, Action}

import scala.concurrent.Await
import scala.concurrent.duration._

class SiteMapController (postDao: PostDao) extends Controller{

  private val config = ConfigFactory.load()

  private val logger = Logger("[SiteMapController]")
  private val currentHost = config.getString("current.host")

  def getSiteMap = Action{
    implicit request =>
      logger.info(s"Start reading posts ids list from DB")

      val postIds = Await.result(postDao.getSitemapIds, 5 seconds)


      logger.info(s"${postIds.size} ids are read from DB")

      Ok(buildSitemapXML(postIds))
  }


  private def buildSitemapXML(ids: List[String]): String ={
    s"""<?xml version="1.0" encoding="UTF-8"?>
      |<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">

        ${ids.map(id => s"| <url> \n <loc>$currentHost/post/$id</loc> \n </url>").mkString("\n")}

      |</urlset>""".stripMargin
  }
}