package domain

import auth.SocialUser
import util.IdGenerator

case class Comment(id: Option[String], body: String, author: SocialUser, postId: String, timestamp: Option[Long])

object CommentConverter{
  implicit def commentToWrapper(comment: Comment) = new CommentWrapper(comment)
}

class CommentWrapper(comment: Comment){
  def toUIComment: UIComment = {
    val profile = comment.author.profile
    val uid = IdGenerator.computeUID(profile.providerId, profile.userId)

    UIComment(id = comment.id,
              body = comment.body,
              authorPic = profile.avatarUrl.getOrElse(""),//TODO: default pic
              authorName = profile.fullName.getOrElse(""), //funny name
              authorUid = uid,
              postId = comment.postId,
              timestamp = comment.timestamp
    )
  }
}

case class UIComment(id: Option[String], body: String, authorPic: String,
                     authorName: String, authorUid: String, postId: String,
                     timestamp: Option[Long])

