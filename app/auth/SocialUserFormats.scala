package auth

import play.api.libs.json.Json
import securesocial.core._
import securesocial.core.OAuth2Info
import securesocial.core.BasicProfile
import securesocial.core.PasswordInfo
import securesocial.core.OAuth1Info

object SocialUserFormats {

  implicit val OAuth1InfoFormat = Json.format[OAuth1Info]
  implicit val OAuth2InfoFormat = Json.format[OAuth2Info]
  implicit val authenticationMethodFormat = Json.format[AuthenticationMethod]
  implicit val passwordInfoFormat = Json.format[PasswordInfo]
  implicit val basicProfileFormat = Json.format[BasicProfile]
  implicit val socialUserFormat = Json.format[SocialUser]

}
