package domain

import java.util.Date

/**
 * Created by akirillov on 8/14/14.
 */
case class Post(id: Option[String] = None, isDraft: Boolean = true, title: String, body: List[DataBlock], displayedDate: Option[String], date: Long, tags: List[String] = List(), comments: List[Comment] = List())

case class PostPreview(id: String, title: String, datePublished: String, blocks: List[DataBlock], tags: List[String] = List())

case class DataBlock(`type`: String, data: String)

object PostPreview{
  def fromPost(post: Post) = new PostPreview(id = post.id.getOrElse("-1"), title = post.title, datePublished = post.displayedDate.getOrElse("infinity"), blocks = post.body.take(2), tags = post.tags)
}