package controllers

import securesocial.core._
import play.api.mvc.{BodyParsers, RequestHeader}
import auth.{SecureSocialAuth, SocialUser}
import play.api.libs.json.Json
import domain.ViewPage
import util.JsonPostTransformer
import service.PostService
import play.api.Logger
import metrics.ApplicationMetrics._

class PostController(postService: PostService, override implicit val env: RuntimeEnvironment[SocialUser])
  extends securesocial.core.SecureSocial[SocialUser] with SecureSocialAuth{

  val logger = Logger("[PostController]")

  def index(pageNum: Option[Int]) = UserAwareAction { implicit request =>
    val context = postListReadTime.time()

    val previews = postService.getPosts(pageNum)
    val postPage = postService.createViewPage("/", previews, pageNum)

    context.stop()

    Ok(views.html.index(request.user)(previews.take(ViewPage.PageSize), postPage))
  }

  def postEditPage(uid: Option[String]) = SecuredAction { implicit request =>
    logger.info("Entered post edit controller method")
    if(uid.isEmpty){
      logger.info("Post UID not specified, creating new post")

      Ok(views.html.editpost(None, None, None, None)(Some(request.user)))

    } else {

      postService.getPostById(uid.get).fold {
        val message = s"Post with uid $uid not found!"
        logger.error(message)
        BadRequest(message)
      }({ post =>
        val message = s"Post with uid $uid found, processing to edit"

        val tags = if (post.tags.isEmpty) None else Some(Json.toJson(post.tags).toString)

        Ok(views.html.editpost(post.id, Some(post.title), Some(JsonPostTransformer.buildSirTrevorBlocks(post).toString()), tags)(Some(request.user)))
      })

    }
  }

  def postViewPage(uid: String) = UserAwareAction { implicit request =>
    val context = singlePostReadTime.time()

    postService.getPostById(uid).fold {
      logger.error(s"Post with UID [$uid] not found!")
      context.stop()

      Redirect("/")
    }({ post =>
      context.stop()

      Ok(views.html.post(post)(request.user))
    })
  }

  def publishPost(uid: String) = SecuredAction { implicit request =>
    postService.publishPost(uid) match {
      case Left(x) =>
        logger.error(x)
        BadRequest(x)
      case Right(x) => Redirect("/")
    }
  }

  def deletePost(uid: String) = SecuredAction { implicit request =>
    postService.deletePostById(uid.trim)
    Ok("post deleted")
  }

  def getDraftsPage(pageNum: Option[Int]) = SecuredAction { implicit request =>
    val context = postListReadTime.time()

    val previews = postService.getDrafts(pageNum)
    val postPage = postService.createViewPage("/drafts", previews, pageNum)

    context.stop()
    Ok(views.html.index(Some(request.user))(previews.take(ViewPage.PageSize), postPage))
  }


  def getPostsByTag(tag: String, pageNum: Option[Int]) = UserAwareAction { implicit request =>
    val context = postListReadTime.time()
    val previews = postService.getPostsByTag(tag, pageNum)
    val postPage = postService.createViewPage(s"/post/tag/$tag", previews, pageNum)

    context.stop()
    Ok(views.html.index(request.user)(previews.take(ViewPage.PageSize), postPage))
  }
  
  def createPost = SecuredAction(BodyParsers.parse.json) {
    implicit request =>
      
      postService.saveJsonPost(request.body) match {
        case Left(errorMessage) =>
          logger.error(errorMessage)
          BadRequest(Json.obj("status" -> "KO", "message" -> errorMessage))
        case Right(postId) => Ok(Json.obj("status" -> "OK", "pid" -> postId))
      }
      
  }

  def updatePost(uid: String) = SecuredAction(BodyParsers.parse.json) {
    implicit request =>

      postService.updateExistingPost(uid, request.body) match {
        case Left(errorMessage) =>
          logger.error(s"Post with UID [$uid] not found!")
          BadRequest(Json.obj("status" -> "KO", "message" -> errorMessage))
        case Right(postId) => Ok(Json.obj("status" -> "OK", "pid" -> postId))
      }
      
  }
}