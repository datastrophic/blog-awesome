package util

import domain.{Comment, Post}
import java.util._
import db.{CommentBucketClient, ReactiveCouchbaseClient}
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.List
import scala.Some
import domain.Post
import dao.PostDao

object PostHelper{

  def generatePublishedPosts(amount: Int): List[Post] = {
    generateDrafts(amount) map (post => post.copy(isDraft = false))
  }

  def generateDrafts(amount: Int): List[Post] = {
    (0 to amount-1) map { i =>
      Thread.sleep(5)

      createBlankPost

    } toList
  }

  def generateTaggedPosts(tags: List[String], amount: Int): List[Post] = {
    (0 to amount-1) map { i =>
      Thread.sleep(5)

      createPostWithTags(tags)

    } toList
  }

  def createPostWithTag(tag: String): Post = createPublishedPost.copy(tags = List(tag))

  def createPostWithTags(tags: List[String]): Post = createPublishedPost.copy(tags = tags)

  def createPublishedPost = createBlankPost.copy(isDraft = false)

  def createDraftPost = createBlankPost

  def createBlankPost = Post(id = Some(generateUid), title = "sample", body = Nil, displayedDate = None, date = new Date().getTime)

  def generateUid = UUID.randomUUID().toString

  def main(args: Array[String]){
    implicit val ec = ExecutionContext.Implicits.global

    try {
      Await.result(new PostDao().bucket.flush(), 15 seconds)
    } catch {
      case e: UnsupportedOperationException => println(e.getMessage)
    }
  }
}
