package service

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import dao.{TagDao, PostDAO}
import domain.{ViewPage, PostPreview, Post}
import play.api.libs.json.JsValue
import util.{StringAndDateUtils, JsonPostTransformer}
import java.util.Date

object PostService {

  def getPostById(uid: String): Option[Post] = {
    Await.result(PostDAO.get(uid), 5 seconds)
  }

  def getPosts(pageNum: Option[Int]): List[PostPreview] = {
    getPostPreviews(PostDAO.findSubmittedPosts(pageNum.getOrElse(1)))
  }

  def getDrafts(pageNum: Option[Int]): List[PostPreview] = {
    getPostPreviews(PostDAO.findDrafts(pageNum.getOrElse(1)))
  }

  def getPostsByTag(tag: String, pageNum: Option[Int]): List[PostPreview] = {
    getPostPreviews(PostDAO.findPostsByTag(tag, pageNum.getOrElse(1)))
  }

  def deletePostById(uid: String) = PostDAO.delete(uid)

  private def getPostPreviews(awaitablePosts: Future[List[Post]]): List[PostPreview] = {
    Await.result(awaitablePosts, 5 seconds) map { post => PostPreview.fromPost(post)}
  }

  def saveJsonPost(json: JsValue): Either[String,String] = {
    val newPost = JsonPostTransformer.createPostFromJson(json)

    newPost map { p =>
      val generatedUID = StringAndDateUtils.generateUID(p.title)
      val dateAsMillis = new Date().getTime

      PostDAO.save(generatedUID, newPost.get.copy(id = Some(generatedUID), isDraft = true, displayedDate = StringAndDateUtils.getCurrentDateAsString, date = dateAsMillis))

      Right(generatedUID)
    } getOrElse Left(s"Json hasn't been parsed correctly:\n${json.toString}")
  }

  def updateExistingPost(uid: String, json: JsValue): Either[String,String] = {
    val newPost = JsonPostTransformer.createPostFromJson(json)

    newPost map { p =>
      val generatedUID = StringAndDateUtils.generateUID(p.title)

      getPostById(uid.trim) map { savedPost =>
        PostDAO.save(generatedUID, newPost.get.copy(id = Some(generatedUID), displayedDate = savedPost.displayedDate, date = savedPost.date))

        if (uid != generatedUID) {
          PostDAO.delete(uid)
        }

        Right(generatedUID)
      } getOrElse Left(s"Post with uid:${uid.trim} not found!")
    } getOrElse Left(s"Json hasn't been parsed correctly:\n${json.toString}")
  }

  def publishPost(uid: String): Either[String, Boolean] = {
    getPostById(uid) map { post =>
      val newPost = post.copy(isDraft = false)

      PostDAO.save(uid, newPost)

      TagDao.mergeTags(post.tags) //! happens only when post is being published to minimize amount of trash tags

      Right(true)
    } getOrElse Left(s"Post with uid: $uid not found!")
  }

  def createViewPage(sourceURL: String, previews: List[PostPreview], pageNum: Option[Int]): ViewPage = {

    pageNum match {
      case Some(page) => {
        val prev = if(page == 2) "" else s"?page=${page-1}"

        if(previews.size == ViewPage.PageSize){
          ViewPage(previous = Some(sourceURL + prev), next = Some(sourceURL+s"?page=${page+1}"))
        } else {
          ViewPage(previous = Some(sourceURL + prev), next = None)
        }

      }
      case None => {
        if(previews.size == ViewPage.PageSize){
          ViewPage(previous = None, next = Some(sourceURL+s"?page=2"))
        } else {
          ViewPage(previous = None, next = None)
        }
      }
    }


//    (pageNum, previews.size <= ViewPage.PageSize) match {
//      //in the middle
//      case (Some(page: Int), true) =>
//        val prev = if(page == 2) "" else s"?page=${page-1}"
//        ViewPage(previous = Some(sourceURL + prev), next = None)
//      //last page
//      case (Some(page: Int), false) =>
//        val prev = if(page == 2) "" else s"?page=${page-1}"
//        ViewPage(previous = Some(sourceURL + prev), next = Some(sourceURL+s"?page=${page+1}"))
//      //no posts around
//      case (None, true) => ViewPage(previous = None, next = None)
//      //first page
//      case (None, false) => ViewPage(previous = None, next = Some(sourceURL+s"?page=2"))
//    }
  }
}
