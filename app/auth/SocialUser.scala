package auth

import securesocial.core.BasicProfile

case class SocialUser(id: String, profile: BasicProfile, isAdmin: Boolean, identities: List[BasicProfile])
