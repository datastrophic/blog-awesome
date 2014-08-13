package auth

import db.ReactiveCouchbaseClient
import securesocial.core.services.{SaveMode, UserService}
import securesocial.core.providers.MailToken
import scala.concurrent.{ExecutionContext, Future}
import securesocial.core.{BasicProfile, PasswordInfo}

/**
 * Created by akirillov on 8/13/14.
 */
class SocialUserService extends  UserService[SocialUser] with ReactiveCouchbaseClient{

 import SocialUserFormats._
 implicit val ec = ExecutionContext.Implicits.global

  override def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {

    executeWithBucket(bucket => {
      bucket.get[SocialUser](s"$providerId::$userId")
    }).map(customUser => customUser.map(user => user.profile))

  }

  override def save(profile: BasicProfile, mode: SaveMode): Future[SocialUser] = {
    val key = s"${profile.providerId}::${profile.userId}"

    executeWithBucket(bucket => {

      bucket.get[SocialUser](key)

    }).map(user => {

      if(user.isDefined) user.get

      else{
        val newUser = SocialUser(profile, isAdmin = false, List(profile))

        executeWithBucket(bucket =>
          bucket.set[SocialUser](s"${profile.providerId}::${profile.userId}", newUser)
        )

        newUser
      }
    })
  }


  //Don't need it now
  override def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = ???

  override def deleteToken(uuid: String): Future[Option[MailToken]] = ???

  override def link(current: SocialUser, to: BasicProfile): Future[SocialUser] = ???

  override def passwordInfoFor(user: SocialUser): Future[Option[PasswordInfo]] = ???

  override def findToken(token: String): Future[Option[MailToken]] = ???

  override def deleteExpiredTokens(): Unit = ???

  override def updatePasswordInfo(user: SocialUser, info: PasswordInfo): Future[Option[BasicProfile]] = ???

  override def saveToken(token: MailToken): Future[MailToken] = ???
}