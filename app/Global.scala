import auth.{SocialUserService, SocialUser, CustomEventListener}
import com.codahale.metrics.JmxReporter
import metrics.ApplicationMetrics
import play.api.{Application, GlobalSettings}
import play.api.mvc._
import scala.concurrent.Future
import play.api.mvc.Results._
import java.lang.reflect.Constructor
import securesocial.core.RuntimeEnvironment

object Global extends GlobalSettings{

  /**
   * The runtime environment for this sample app.
   */
  object MyRuntimeEnvironment extends RuntimeEnvironment.Default[SocialUser] {
    override lazy val userService: SocialUserService = new SocialUserService()
    override lazy val eventListeners = List(new CustomEventListener())
  }

  override def doFilter(next: EssentialAction): EssentialAction = {
    Filters(super.doFilter(next), AccessLoggingFilter)
  }

  /**
   * An implementation that checks if the controller expects a RuntimeEnvironment and
   * passes the instance to it if required.
   *
   * This can be replaced by any DI framework to inject it differently.
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    val instance  = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[SocialUser]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(MyRuntimeEnvironment)
    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    implicit val env = Global.MyRuntimeEnvironment
    implicit val header = request
    Future.successful(
      NotFound(views.html.notfound(None))
    )
  }

  override def onStart(app: Application): Unit = {
    super.onStart(app)

    val jmxReporter = JmxReporter.forRegistry(ApplicationMetrics.metricRegistry).build()
    jmxReporter.start()
  }
}
