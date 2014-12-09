package domain

import auth.SocialUser
import util.IdGenerator

//case class Comment(id: Option[String] = None, body: String, author: Option[SocialUser] = None, postId: String, timestamp: Option[Long] = None)

case class Comment(id: Option[String] = None, body: String, authorPic: String,
                     authorName: String, authorUid: String, postId: String,
                     timestamp: Option[Long] = None)

