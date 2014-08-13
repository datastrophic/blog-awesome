package controllers

import securesocial.controllers.BaseLoginPage
import play.api.mvc.{RequestHeader, AnyContent, Action}
import play.api.Logger
import securesocial.core.{RuntimeEnvironment, IdentityProvider}
import securesocial.core.services.RoutesService
import securesocial.core.providers.UsernamePasswordProvider
import auth.SocialUser

class CustomLoginController(implicit override val env: RuntimeEnvironment[SocialUser]) extends BaseLoginPage[SocialUser] {
  override def login: Action[AnyContent] = {
    Logger.debug("using CustomLoginController")
    UserAwareAction { implicit request =>
      Ok(views.html.login(UsernamePasswordProvider.loginForm))
    }
  }
}


class CustomRoutesService extends RoutesService.Default {
  override def loginPageUrl(implicit req: RequestHeader): String = controllers.routes.CustomLoginController.login().absoluteURL(IdentityProvider.sslEnabled)
}