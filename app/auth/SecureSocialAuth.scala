package auth

import securesocial.core.Authorization
import play.api.mvc.RequestHeader

trait SecureSocialAuth {
  // An Authorization implementation that only authorizes uses that logged in using twitter
  case class WithProvider(provider: String) extends Authorization[SocialUser] {
    def isAuthorized(user: SocialUser, request: RequestHeader) = {
      user.isAdmin
    }
  }
}
