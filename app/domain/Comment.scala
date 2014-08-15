package domain

import auth.SocialUser

/**
 * Created by akirillov on 8/14/14.
 */
case class Comment(id: String, body: String, author: SocialUser) extends HasId
