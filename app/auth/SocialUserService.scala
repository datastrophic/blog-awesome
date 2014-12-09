package auth

import db.{UserBucketClient, ReactiveCouchbaseClient}
import securesocial.core.services.{SaveMode, UserService}
import securesocial.core.providers.MailToken
import scala.concurrent.{ExecutionContext, Future}
import securesocial.core.{BasicProfile, PasswordInfo}
import util.IdGenerator

class SocialUserService extends UserService[SocialUser] with UserBucketClient{

 import SocialUserFormats._

  override def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    val uid = IdGenerator.computeUID(providerId, userId)

    getUserByKey(uid).map(customUser => customUser.map(user => user.profile))

  }

  override def save(profile: BasicProfile, mode: SaveMode): Future[SocialUser] = {
    val uid = IdGenerator.computeUID(profile.providerId, profile.userId)

    getUserByKey(uid).map(user => {

      if(user.isDefined) user.get

      else{
        val newUser = SocialUser(uid, profile, isAdmin = false, List(profile))

        executeWithBucket(bucket =>
          bucket.set[SocialUser](uid, newUser)
        )

        newUser
      }
    })
  }

  def getUserByKey(key: String): Future[Option[SocialUser]] = {
    executeWithBucket(bucket => {
      bucket.get[SocialUser](key)
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