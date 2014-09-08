package util

import domain.{Post, DataBlock}
import play.api.libs.json._
import play.api.libs.json

/**
 * Created by akirillov on 8/21/14.
 */
object JsonTransformer {

  /**
   * Idea: as Sir Trevor can't be used in read-only mode (?) the next outcomes arise:
   * 1. Sir Trevor has it's own representation of data
   * 2. Post view form will use markdown parser and Sir Trevors format is incompatible with it
   * 3. Reading and writing to data source needs to be typesafe
   *
   * This leads to 3 domain representation modes: st-editor(for post edition), client view(markdown-ready) and domain
   * model classes which are used for generation of both views
   *
   * Logic:
   *
   * 1. st -> [ domain model ] -> database
   *
   * 2. database -> st JSON
   *
   * 3. database -> markdown json
   *
   *
   *
   * IDEA: store `data` field of type JsValue
   *
   * Predefined READS https://www.playframework.com/documentation/2.3.1/ScalaJsonCombinators
  */

  private val arrayTransformer = (__ \ 'data).json.pick[JsArray]
  private val tagsTransformer = (__ \ 'tags).json.pick[JsArray]
  private val titleTransformer = (__ \ 'title).json.pick[JsString]
  private val typeTransformer = (__ \ 'type).json.pick[JsString]
  private val textTransformer = (__ \ 'data \ 'text).json.pick[JsString]
  private val imageTransformer = (__ \ 'data \ 'file \ 'url).json.pick[JsString]
  private val gistTransformer = (__ \ 'data \ 'id).json.pick[JsString]

  def buildSirTrevorBlocks(post: Post): JsValue =
    Json.obj(
      "data" -> post.body.map(createBlockJson)
    )

  private def createBlockJson(dataBlock: DataBlock): JsValue = {
    dataBlock.`type` match {
      case "text" => createTextBlock(dataBlock)
      case "image" => createImageBlock(dataBlock)
      case "gist" => createGistBlock(dataBlock)
      case _ => Json.obj()
    }
  }

  private def createTextBlock(dataBlock: DataBlock): JsValue = {
    Json.obj(
      "type" -> "text",
      "data" -> Json.obj(
        "text" -> dataBlock.data
      )
    )
  }

  private def createImageBlock(dataBlock: DataBlock): JsValue = {
    Json.obj(
      "type" -> "image",
      "data" -> Json.obj(
        "file" -> Json.obj(
          "url" -> dataBlock.data
        )
      )
    )
  }

  private def createGistBlock(dataBlock: DataBlock): JsValue = {
    Json.obj(
      "type" -> "gist",
      "data" -> Json.obj(
        "id" -> dataBlock.data
      )
    )
  }

  def createPostFromJson(json: JsValue): Option[Post] = {
    getStringValue(json, titleTransformer) flatMap {
      title =>

        val extractedTags = extractTags(json).getOrElse(List())

        extractBlocksFromPostJson(json) map {
              //TODO: fix date creation in outer blocks of code
          blocks => Post(title = title, body = blocks, displayedDate = None, tags = extractedTags, comments = Nil, date = -1L)
        }
    }
  }

  def extractTags(json: JsValue): Option[List[String]] = {
    json.transform(tagsTransformer) match {
      case s: JsSuccess[JsArray] =>
        val arr = s.get
        Some(arr.value.map(v => v.as[String]).toList)
      case error: JsError => None
    }
  }

  def extractBlocksFromPostJson(json: JsValue): Option[List[DataBlock]] = {
    json.transform(arrayTransformer) match {
      case s: JsSuccess[JsArray] =>
        val arr = s.get
        Some(arr.value.map(createDataBlock).toList.flatten)
      case error: JsError => None
    }
  }

  private def createDataBlock(jsValue: JsValue): Option[DataBlock] = {
    getType(jsValue) flatMap {
      case "text" => getText(jsValue) flatMap (text => Some(DataBlock("text", text)))
      case "image" => getImage(jsValue) flatMap (url => Some(DataBlock("image", url)))
      case "gist" => getGist(jsValue) flatMap (gistId => Some(DataBlock("gist", gistId)))
      case _ => None
    }
  }

  private def getStringValue(jsValue: JsValue, reads: Reads[JsString]): Option[String] = {
    jsValue.transform(reads) match {
      case s: JsSuccess[JsString] => Some(s.get.value)
      case _ => None
    }
  }

  private def getImage(jsValue: JsValue): Option[String] = getStringValue(jsValue, imageTransformer)
  private def getText(jsValue: JsValue): Option[String] = getStringValue(jsValue, textTransformer)
  private def getType(jsValue: JsValue): Option[String] = getStringValue(jsValue, typeTransformer)
  private def getGist(jsValue: JsValue): Option[String] = getStringValue(jsValue, gistTransformer)


  def main(args: Array[String]): Unit = {
    val sample = Json.parse("{\"title\":\"test\",\"data\":[{\"type\":\"text\",\"data\":{\"text\":\"any\"}},{\"type\":\"image\",\"data\":{\"file\":{\"url\":\"werwerwer\"}}}], \"tags\":[\"one\", \"two\"]}")

    extractTags(sample) foreach println
  }
}
