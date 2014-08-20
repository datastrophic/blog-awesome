package domain

import java.util.Date

/**
 * Created by akirillov on 8/14/14.
 */
case class Post(id: String, title: String, preview: String, body: String, date: String, tags: List[String] = List(), comments: List[Comment] = List())

case class PostDTO(title: String, data: List[DataBlock])

case class DataBlock(`type`: String, data: BlockContent)

case class BlockContent(text: String)