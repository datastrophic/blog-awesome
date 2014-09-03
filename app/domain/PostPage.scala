package domain

/**
 * Created by akirillov on 9/2/14.
 */
case class PostPage(previous: Option[String] = None, next: Option[String] = None)

object PostPage{
  val PageSize = 2
}