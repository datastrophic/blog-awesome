package exception

/**
 * Created by akirillov on 4/22/14.
 */
class ConfigurationException(msg: String) extends RuntimeException(msg)

object ConfigurationException{
  def apply(msg: String): ConfigurationException = new ConfigurationException(msg)
}