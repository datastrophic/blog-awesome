package metrics

import akka.actor.Actor

class HealthCheckActor extends Actor {
  def receive = {
    case "tick" =>
      CouchbaseHealthCheck.check()
  }
}