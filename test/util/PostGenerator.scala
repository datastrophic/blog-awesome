package util

import domain.{Comment, Post}
import java.text.SimpleDateFormat
import java.util.{Date, Calendar, GregorianCalendar, Locale}

/**
 * Created by akirillov on 8/14/14.
 */
object PostGenerator {
    def generate(amount: Int): List[Post] = {
      (0 to amount-1) map { i =>
        Thread.sleep(5)

        createBlankPost.copy(id = Some(s"${i.toString}_${new Date().getTime}"))

      } toList
    }



  def createPostWithTag(tag: String): Post = createPublishedPost.copy(tags = List(tag))

  def createPublishedPost = createBlankPost.copy(isDraft = false)

  def createDraftPost = createBlankPost

  def createBlankPost = Post(title = "sample", body = Nil, displayedDate = None, date = new Date().getTime)
}
