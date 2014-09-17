package controllers

import securesocial.core.{Authorization, RuntimeEnvironment}
import auth.SocialUser
import play.api.mvc.RequestHeader
import play.api.libs.json.Json
import play.api.{Play, Logger}
import java.security.SecureRandom
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import java.io.File
import java.math.BigInteger
import scalax.file.Path
import play.api.Play.current
import com.typesafe.config.ConfigFactory

/**
 * Created by akirillov on 8/20/14.
 */
class ImageController (override implicit val env: RuntimeEnvironment[SocialUser]) extends securesocial.core.SecureSocial[SocialUser] {
  private val logger = Logger("[ImageController]")

  private val config = ConfigFactory.load()

  private val currentHost: String = config.getString("current.host")
  private val imageFolder: String = config.getString("image.system.path")

  val pathPrefix = if (Play.isProd) s"$imageFolder" else s"/tmp/playblog"

  private val random = new SecureRandom()

  def uploadImage = SecuredAction(parse.multipartFormData) {
    implicit request =>
      println(request.body)
      request.body.file("attachment[file]").map {
        picture =>
          logger.info("Got service image upload request")
              val url = saveUploadedImage(picture)
              Ok(Json.obj("file" -> Json.obj("url" -> url)))
      }.getOrElse {
        BadRequest("Errors occurred")
      }
  }

  private def saveUploadedImage(filePart: FilePart[TemporaryFile]): String = {
    val fileName = generateNewFileName(filePart.filename)
    val path = s"images/$fileName"

    saveImage(filePart, path)

    val url = s"$currentHost/$path"
    logger.info(s"Generated url: $url")
    url
  }

  private def generateNewFileName(oldFileName: String): String = {
    val randomUid = new BigInteger(130, random).toString(32)
    randomUid + oldFileName.substring(oldFileName.lastIndexOf("."))
  }

  def saveImage(filePart: FilePart[TemporaryFile], path: String) = {
    val fileName = s"$pathPrefix/$path"
    logger.info(s"Saving image $fileName")

    val scalaxPath: Path = Path.fromString(fileName)
    scalaxPath.createFile(failIfExists=false)

    filePart.ref.moveTo(new File(scalaxPath.toURI), replace = true)
  }

  // An Authorization implementation that only authorizes uses that logged in using twitter
  case class WithProvider(provider: String) extends Authorization[SocialUser] {
    def isAuthorized(user: SocialUser, request: RequestHeader) = {
      user.profile.providerId == provider
    }
  }

}