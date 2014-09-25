package domain

import auth.SocialUser

case class Comment(id: String, body: String, author: SocialUser)
