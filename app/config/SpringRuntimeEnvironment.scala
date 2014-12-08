package config

import securesocial.core.RuntimeEnvironment
import securesocial.core.services.UserService
import auth.SocialUser

class SpringRuntimeEnvironment(wiredService: UserService[SocialUser]) extends RuntimeEnvironment.Default[SocialUser]{
  override lazy val userService = wiredService
}