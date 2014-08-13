package auth

import securesocial.core.BasicProfile

/**
 * Created by akirillov on 8/13/14.
 */
// a simple User class that can have multiple identities
case class SocialUser(profile: BasicProfile, isAdmin: Boolean, identities: List[BasicProfile])
