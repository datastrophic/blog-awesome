package domain

/**
 * Contains the information about current, previous and next view page
 */
case class ViewPage(previous: Option[String] = None, next: Option[String] = None)

object ViewPage{
  val PageSize = 10
}