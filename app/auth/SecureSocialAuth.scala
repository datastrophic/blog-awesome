package auth

import securesocial.core.Authorization
import play.api.mvc.RequestHeader

/**
 * Created by akirillov on 9/17/14.
 */
trait SecureSocialAuth {
  // An Authorization implementation that only authorizes uses that logged in using twitter
  case class WithProvider(provider: String) extends Authorization[SocialUser] {
    def isAuthorized(user: SocialUser, request: RequestHeader) = {
      user.isAdmin
    }
  }
}
