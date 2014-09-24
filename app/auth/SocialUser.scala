package auth

import securesocial.core.BasicProfile

case class SocialUser(profile: BasicProfile, isAdmin: Boolean, identities: List[BasicProfile])
