package util

import org.springframework.scala.context.function.FunctionalConfigApplicationContext
import config.ApplicationConfig

object SpringContextHelper {
  lazy val springContext = FunctionalConfigApplicationContext(classOf[ApplicationConfig])
}