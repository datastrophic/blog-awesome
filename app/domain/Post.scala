package domain

import java.util.Date

/**
 * Created by akirillov on 8/14/14.
 */
case class Post(id: Option[String] = None, isDraft: Boolean = true, title: String, body: List[DataBlock], date: Option[String], tags: List[String] = List(), comments: List[Comment] = List())

case class Preview(id: String, title: String, datePublished: String, blocks: List[DataBlock], tags: List[String] = List())

case class PostDTO(title: String, blocks: List[DataBlock])

case class DataBlock(`type`: String, data: String)

object Preview{
  def fromPost(post: Post) = new Preview(id = post.id.getOrElse("-1"), title = post.title, datePublished = post.date.getOrElse("infinity"), blocks = post.body.take(2), tags = post.tags)
}