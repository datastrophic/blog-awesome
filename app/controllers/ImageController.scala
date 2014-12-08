package controllers

import securesocial.core.{SecureSocial, Authorization, RuntimeEnvironment}
import auth.{SecureSocialAuth, SocialUser}
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

//class ImageController(userService: UserService, override implicit val env: RuntimeEnvironment[User]) extends SecureSocial[User] with SecureSocialAuth {

class ImageController (override implicit val env: RuntimeEnvironment[SocialUser]) extends SecureSocial[SocialUser]  with SecureSocialAuth{

  private val logger = Logger("[ImageController]")

  private val config = ConfigFactory.load()

  private val currentHost: String = config.getString("current.host")
  private def imageFolder: String = {
    val confPath = config.getString("image.system.path")

    if(confPath.endsWith("/")) confPath
    else s"$confPath/"
  }

  val pathPrefix = if (Play.isProd) s"$imageFolder" else s"/tmp/octopus/images"

  private val random = new SecureRandom()

  def uploadImage = SecuredAction(parse.multipartFormData) {
    implicit request =>
      request.body.file("attachment[file]").fold(BadRequest("Errors occurred")){picture =>
        val url = saveUploadedImage(picture)
        Ok(Json.obj("file" -> Json.obj("url" -> url)))
      }
  }

  private def saveUploadedImage(filePart: FilePart[TemporaryFile]): String = {
    logger.info("Saving image")
    val fileName = generateNewFileName(filePart.filename)

    saveImage(filePart, fileName)

    val url = s"$currentHost/images/$fileName"
    logger.info(s"Image saved, generated url: $url")
    url
  }

  private def generateNewFileName(oldFileName: String): String = {
    val randomUid = new BigInteger(130, random).toString(32)
    randomUid + oldFileName.substring(oldFileName.lastIndexOf("."))
  }

  def saveImage(filePart: FilePart[TemporaryFile], path: String) = {
    val fileName = s"$pathPrefix/$path"
    logger.info(s"Writing file $fileName")

    val scalaxPath: Path = Path.fromString(fileName)
    scalaxPath.createFile(failIfExists=false)

    filePart.ref.moveTo(new File(scalaxPath.toURI), replace = true)

    logger.info(s"File $fileName saved to disk")
  }
}