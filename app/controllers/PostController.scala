package controllers

import securesocial.core._
import play.api.mvc.{BodyParsers, RequestHeader}
import auth.{SecureSocialAuth, SocialUser}
import play.api.libs.json.Json
import domain.ViewPage
import util.JsonPostTransformer
import service.PostService

class PostController(override implicit val env: RuntimeEnvironment[SocialUser])
  extends securesocial.core.SecureSocial[SocialUser] with SecureSocialAuth{

  def index(pageNum: Option[Int]) = UserAwareAction { implicit request =>
    val previews = PostService.getPosts(pageNum)
    val postPage = PostService.createViewPage("/", previews, pageNum)

    Ok(views.html.index(request.user)(previews.take(ViewPage.PageSize), postPage))
  }

  def postEditPage(uid: Option[String]) = SecuredAction { implicit request =>
    if(uid.isEmpty){

      Ok(views.html.editpost(None, None, None, None)(Some(request.user)))

    } else {

      PostService.getPostById(uid.get).fold(
        BadRequest(s"Post with uid $uid not found!")
      )({ post =>
        val tags = if (post.tags.isEmpty) None else Some(Json.toJson(post.tags).toString)

        Ok(views.html.editpost(post.id, Some(post.title), Some(JsonPostTransformer.buildSirTrevorBlocks(post).toString()), tags)(Some(request.user)))
      })

    }
  }

  def postViewPage(uid: String) = UserAwareAction { implicit request =>
    PostService.getPostById(uid).fold(
      Redirect("/")
    )({ post =>
      Ok(views.html.post(post)(request.user))
    })
  }

  def publishPost(uid: String) = SecuredAction { implicit request =>
    PostService.publishPost(uid) match {
      case Left(x) => BadRequest(x)
      case Right(x) => Redirect("/")
    }
  }

  def deletePost(uid: String) = SecuredAction { implicit request =>
    PostService.deletePostById(uid.trim)
    Ok("post deleted")
  }

  def getDraftsPage(pageNum: Option[Int]) = SecuredAction { implicit request =>
    val previews = PostService.getDrafts(pageNum)
    val postPage = PostService.createViewPage("/drafts", previews, pageNum)
    
    Ok(views.html.index(Some(request.user))(previews.take(ViewPage.PageSize), postPage))
  }


  def getPostsByTag(tag: String, pageNum: Option[Int]) = UserAwareAction { implicit request =>
    val previews = PostService.getPostsByTag(tag, pageNum)
    val postPage = PostService.createViewPage(s"/post/tag/$tag", previews, pageNum)

    Ok(views.html.index(request.user)(previews.take(ViewPage.PageSize), postPage))
  }
  
  def createPost = SecuredAction(BodyParsers.parse.json) {
    implicit request =>
      
      PostService.saveJsonPost(request.body) match {
        case Left(errorMessage) => BadRequest(Json.obj("status" -> "KO", "message" -> errorMessage))
        case Right(postId) => Ok(Json.obj("status" -> "OK", "pid" -> postId))
      }
      
  }

  def updatePost(uid: String) = SecuredAction(BodyParsers.parse.json) {
    implicit request =>

      PostService.updateExistingPost(uid, request.body) match {
        case Left(errorMessage) => BadRequest(Json.obj("status" -> "KO", "message" -> errorMessage))
        case Right(postId) => Ok(Json.obj("status" -> "OK", "pid" -> postId))
      }
      
  }
}