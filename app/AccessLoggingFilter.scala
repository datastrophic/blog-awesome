import play.api.Logger
import play.api.mvc.{Result, RequestHeader, Filter}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object AccessLoggingFilter extends Filter {

  val accessLogger = Logger("[AccessLogger]")

  def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    val resultFuture = next(request)

    resultFuture.foreach(result => {
      if(!isAsset(request.uri)) {
        val msg = s"method=${request.method} uri=${request.uri} remote-address=${request.remoteAddress}" +
          s" status=${result.header.status}"
        accessLogger.info(msg)
      }
    })

    resultFuture
  }

  def isAsset(path: String): Boolean = {
    path.startsWith("/we") ||
    path.startsWith("/as") ||
    path.startsWith("/im") ||
    path.startsWith("/fa")
  }
}