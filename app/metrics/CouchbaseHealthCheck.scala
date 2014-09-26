package metrics

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import db.ReactiveCouchbaseClient
import play.api.Logger


object CouchbaseHealthCheck extends HealthCheck with ReactiveCouchbaseClient{
  private val HealthCheckKey = "fo::healthcheck"

  private val logger = Logger("[CouchbaseHealthCheck]")

  override def check(): Result = {
    logger.info("Running healthcheck")

    try {

      val result = executeWithBucket {
        bucket => bucket.incrAndGet(HealthCheckKey, 1)
      }

      if(Await.result(result, 5 seconds) != 0){
        logger.info("HealthCheck successful")
        HealthCheck.Result.healthy
      } else {
        logger.warn("HealthCheck unsuccessful")
        HealthCheck.Result.unhealthy("Error during healthcheck counter increment")
      }

    } catch {
      case t: Throwable =>
        logger.error("Error during healthcheck", t)
        HealthCheck.Result.unhealthy(t.getMessage)
    }
  }

  def createKeyIfNotExists() = {
    val result = Await.result(
        executeWithBucket {
          bucket => bucket.get[Int](HealthCheckKey)
        },
      5 seconds)

    result match {
      case Some(t) =>
      case None =>
        Await.result(
          executeWithBucket {
            bucket => bucket.set[Int](HealthCheckKey, 1)
          },
        5 seconds)
    }
  }
}
