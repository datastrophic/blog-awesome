import auth.{SocialUserService, SocialUser, CustomEventListener}
import com.codahale.metrics.JmxReporter
import config.ApplicationConfig
import metrics.ApplicationMetrics
import org.springframework.scala.context.function.FunctionalConfigApplicationContext
import play.api.{Logger, Application, GlobalSettings}
import play.api.mvc._
import scala.concurrent.Future
import play.api.mvc.Results._
import java.lang.reflect.Constructor
import securesocial.core.RuntimeEnvironment

object Global extends GlobalSettings{

  override def doFilter(next: EssentialAction): EssentialAction = {
    Filters(super.doFilter(next), AccessLoggingFilter)
  }

  private lazy val context = FunctionalConfigApplicationContext(classOf[ApplicationConfig])
  private lazy val env = context.getBean("runtimeEnvironment").asInstanceOf[RuntimeEnvironment[SocialUser]]


  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    try {
      context.getBean(controllerClass)
    } catch {
      case t: Throwable => getSecureSocialController(controllerClass)
    }
  }

  private def getSecureSocialController[A](controllerClass: Class[A]): A = {
    val instance = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[SocialUser]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(env)
    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    implicit val environment = env
    implicit val header = request
    Future.successful(
      NotFound(views.html.notfound(None))
    )
  }

  override def onStart(app: Application): Unit = {
    super.onStart(app)
    Logger.info("Application has started")

    context.start()
    Logger.info("Spring context started")

    val jmxReporter = JmxReporter.forRegistry(ApplicationMetrics.metricRegistry).build()
    jmxReporter.start()
  }
}
