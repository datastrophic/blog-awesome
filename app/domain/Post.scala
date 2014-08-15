package domain

import java.util.Date

/**
 * Created by akirillov on 8/14/14.
 */
case class Post(id: String, title: String, body: String, date: String, tags: List[String] = List(), comments: List[Comment] = List())
  extends HasId
