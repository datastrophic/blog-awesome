package controllers

import securesocial.core._
import play.api.mvc.{BodyParsers, RequestHeader}
import auth.{SecureSocialAuth, SocialUser}
import service.{CommentService, PostService}
import play.api.Logger
import metrics.ApplicationMetrics._
import domain.{Comment, ViewPage}
import play.api.libs.json.Json
import domain.JsonFormats._

class CommentController(commentService: CommentService, override implicit val env: RuntimeEnvironment[SocialUser])
  extends securesocial.core.SecureSocial[SocialUser] with SecureSocialAuth{

  val logger = Logger("[PostController]")

  def createComment() = SecuredAction(BodyParsers.parse.json) { implicit request =>
    commentService.createComment(request.body) match {
      case Left(errorMessage) =>
        logger.error(errorMessage)
        BadRequest(Json.obj("status" -> "KO", "message" -> errorMessage))
      case Right(comment) => Ok(comment)
    }
  }

  def updateComment(commentId: String) = SecuredAction(BodyParsers.parse.json) { implicit request =>
    commentService.updateComment(commentId, request.body) match {
      case Left(errorMessage) =>
        logger.error(errorMessage)
        BadRequest(Json.obj("status" -> "KO", "message" -> errorMessage))
      case Right(message) => Ok(Json.obj("status" -> "OK", "message" -> message))
    }
  }

  def deleteComment(commentId: String) = SecuredAction{implicit request =>
    commentService.deleteComment(commentId)
    Ok(Json.obj("status" -> "OK", "message" -> s"Comment with id $commentId scheduled for deletion"))
  }

  def getPostComments(postId: String)= UserAwareAction{implicit request =>
    val comments = commentService.getCommentsByPostId(postId)
//    val comments = getComments
    Ok(Json.toJson(comments))
  }

  private def getComments: List[Comment] = {
    (1 to 5).map(i => Comment(id = Some(i+""), body = s"smart comment $i", authorPic = "http://placehold.it/80",
      authorName = s"Commentor #$i", authorUid = s"uid_$i", postId = s"post_id_$i", displayedDate = Some("11 November 2014"))).toList
  }

}