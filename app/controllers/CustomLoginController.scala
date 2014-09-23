package controllers

import securesocial.controllers.BaseLoginPage
import play.api.mvc.{RequestHeader, AnyContent, Action}
import play.api.Logger
import securesocial.core.{RuntimeEnvironment, IdentityProvider}
import securesocial.core.services.RoutesService
import securesocial.core.providers.UsernamePasswordProvider
import auth.SocialUser

class CustomLoginController(implicit override val env: RuntimeEnvironment[SocialUser]) extends BaseLoginPage[SocialUser]{
  override def login: Action[AnyContent] = Action{ Redirect("/unauthorized") }

  def unauthorized: Action[AnyContent] = Action{ BadRequest("You're not allowed to be here, sorry.") }
}