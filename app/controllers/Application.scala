package controllers

import _root_.java.util.Date
import securesocial.core._
import play.api.mvc.{BodyParsers, RequestHeader}
import auth.SocialUser
import play.api.libs.json.{JsValue, JsError, Json}
import domain.{PostPage, Preview, Post, PostDTO}
import domain.DomainJsonFormats._
import util.{StringAndDateUtils, JsonTransformer}
import dao.{TagDao, PostDAO}
import scala.concurrent.Await
import scala.concurrent.duration._
import play.mvc.Result
import scala.util.Random

class Application(override implicit val env: RuntimeEnvironment[SocialUser]) extends securesocial.core.SecureSocial[SocialUser] {

  def index(page: Option[Int]) = UserAwareAction { implicit request =>
    val previews = Await.result(PostDAO.findSubmittedPosts(page.getOrElse(1)), 5 seconds) map {post => Preview.fromPost(post)}

    val postPage = createPage("/", previews, page)

    Ok(views.html.index(request.user)(previews.take(PostPage.PageSize), postPage))
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

  def drafts(page: Option[Int]) = UserAwareAction { implicit request =>
    val previews = Await.result(PostDAO.findDrafts(page.getOrElse(1)), 5 seconds) map {post => Preview.fromPost(post)}

    val postPage = createPage("/drafts", previews, page)

    Ok(views.html.index(request.user)(previews.take(PostPage.PageSize), postPage))
  }

  def postsByTag(tag: String, page: Option[Int]) = UserAwareAction { implicit request =>
    val previews = Await.result(PostDAO.findPostsByTag(tag, page.getOrElse(1)), 5 seconds) map {post => Preview.fromPost(post)}

    val postPage = createPage(s"/post/tag/$tag", previews, page)

    Ok(views.html.index(request.user)(previews.take(PostPage.PageSize), postPage))
  }
  
  
  private def createPage(source: String, previews: List[Preview], pageNum: Option[Int]) = {

    (pageNum, previews.size <= PostPage.PageSize) match {
      //in the middle
      case (Some(page: Int), true) =>
        val prev = if(page == 2) "" else s"?page=${page-1}"
        PostPage(previous = Some(source + prev), next = None)

      //last page
      case (Some(page: Int), false) =>
        val prev = if(page == 2) "" else s"?page=${page-1}"
        PostPage(previous = Some(source + prev), next = Some(source+s"?page=${page+1}"))
      //no posts around
      case (None, true) => PostPage(previous = None, next = None)
      //first page
      case (None, false) => PostPage(previous = None, next = Some(source+s"?page=2"))
    }
  }

  def createPost = UserAwareAction(BodyParsers.parse.json) {
    implicit request =>
      val newPost = JsonTransformer.createPostFromJson(request.body)

      newPost map {p =>
        val generatedUID = StringAndDateUtils.generateUID(p.title)
        val dateAsMillis = new Date().getTime

        PostDAO.save(generatedUID, newPost.get.copy(id = Some(generatedUID), isDraft = true, displayedDate = StringAndDateUtils.getCurrentDateAsString, date = dateAsMillis))

        Ok(Json.obj("status" -> "OK", "pid" -> generatedUID))
      } getOrElse {
        BadRequest(Json.obj("status" -> "KO", "message" -> "some errors with json structure"))
      }
  }

  def updatePost(uid: String) = UserAwareAction(BodyParsers.parse.json) {
    implicit request =>

      val newPost = JsonTransformer.createPostFromJson(request.body)

      newPost map {p =>
        val generatedUID = StringAndDateUtils.generateUID(p.title)

        Await.result(PostDAO.get(uid.trim), 5 seconds) map { savedPost =>
          PostDAO.save(generatedUID, newPost.get.copy(id = Some(generatedUID), displayedDate = savedPost.displayedDate, date = savedPost.date))
          println(s"in updatePost => uid: $uid\npost: $newPost")

          if(uid != generatedUID){
            PostDAO.delete(uid)
          }
          Ok(Json.obj("status" -> "OK", "pid" -> generatedUID))
        } getOrElse {
          BadRequest(Json.obj("status" -> "KO", "message" -> s"post with uid $uid not found in database"))
        }



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