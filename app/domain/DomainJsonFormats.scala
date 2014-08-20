package domain

import play.api.libs.json.Json
import auth.SocialUserFormats._
/**
 * Created by akirillov on 8/20/14.
 */
object DomainJsonFormats {

  implicit val dataBlockContentFormat = Json.format[BlockContent]
  implicit val dataBlockFormat = Json.format[DataBlock]
  implicit val postDTOFormat= Json.format[PostDTO]
  implicit val commentFormat= Json.format[Comment]
  implicit val postFormat = Json.format[Post]

}
