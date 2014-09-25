/**
 * Copyright 2013-2014 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package auth

import securesocial.core._
import play.api.mvc.{Session, RequestHeader}
import play.api.Logger

class CustomEventListener extends EventListener[SocialUser] {
  private val logger = Logger("[CustomEventListener]")

  def onEvent(event: Event[SocialUser], request: RequestHeader, session: Session): Option[Session] = {
    val eventName = event match {
      case LoginEvent(u) => "login"
      case LogoutEvent(u) => "logout"
      case SignUpEvent(u) => "signup"
      case PasswordResetEvent(u) => "password reset"
      case PasswordChangeEvent(u) => "password change"
    }

    logger.info("Traced %s event for user %s".format(eventName, event.user.profile.userId))
 
    None
  }
}
