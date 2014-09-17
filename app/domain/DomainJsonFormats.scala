package domain

import play.api.libs.json.Json
import auth.SocialUserFormats._

object DomainJsonFormats {

//  implicit val dataBlockContentFormat = Json.format[BlockContent]
  implicit val dataBlockFormat = Json.format[DataBlock]
  implicit val commentFormat= Json.format[Comment]
  implicit val previewFormat= Json.format[PostPreview]
  implicit val postFormat = Json.format[Post]

}
