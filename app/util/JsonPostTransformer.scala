package util

import domain.{Post, DataBlock}
import play.api.libs.json._

/**
 * Idea behind post storage:
 *
 * As Sir Trevor internal representation is Markdown-like, it seems reasonable to store blocks separately
 * specifying type and raw content.
 *
 * For representation in view mode each block data is modified to valid markdown syntax (depending on block type).
 * After that markdown.js is used for appropriate representation.
 *
 * When the data is passed back to Sir Trevor for editing, block data is transformed to be correctly understood by
 * editor.
 *
 * The bottom line is that data stored in raw markdown format and when used is slightly modified for proper displaying.
 */
object JsonPostTransformer {

  private val arrayTransformer = (__ \ 'data).json.pick[JsArray]
  private val tagsTransformer = (__ \ 'tags).json.pick[JsArray]
  private val titleTransformer = (__ \ 'title).json.pick[JsString]
  private val typeTransformer = (__ \ 'type).json.pick[JsString]
  private val textTransformer = (__ \ 'data \ 'text).json.pick[JsString]
  private val listTransformer = (__ \ 'data \ 'text).json.pick[JsString]
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
      case "list" => createListBlock(dataBlock)
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

  private def createListBlock(dataBlock: DataBlock): JsValue = {
    Json.obj(
      "type" -> "list",
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

  private def extractBlocksFromPostJson(json: JsValue): Option[List[DataBlock]] = {
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
      case "list" => getList(jsValue) flatMap (list => Some(DataBlock("list", list)))
      case _ => None
    }
  }

  private def getImage(jsValue: JsValue): Option[String] = getStringValue(jsValue, imageTransformer)
  private def getText(jsValue: JsValue): Option[String] = getStringValue(jsValue, textTransformer)
  private def getType(jsValue: JsValue): Option[String] = getStringValue(jsValue, typeTransformer)
  private def getGist(jsValue: JsValue): Option[String] = getStringValue(jsValue, gistTransformer)
  private def getList(jsValue: JsValue): Option[String] = getStringValue(jsValue, listTransformer)

  private def getStringValue(jsValue: JsValue, reads: Reads[JsString]): Option[String] = {
    jsValue.transform(reads) match {
      case s: JsSuccess[JsString] => Some(s.get.value)
      case _ => None
    }
  }
}
