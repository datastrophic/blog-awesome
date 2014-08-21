package domain

import java.util.Date

/**
 * Created by akirillov on 8/14/14.
 */
case class Post(id: Option[String] = None, isDraft: Boolean = true, title: String, body: List[DataBlock], date: String, tags: List[String] = List(), comments: List[Comment] = List())

case class PostDTO(title: String, blocks: List[DataBlock])

case class DataBlock(`type`: String, data: String)

//case class BlockContent(text: String)