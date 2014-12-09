package util

import domain.{Comment, Post}
import java.util._
import db.CommentBucketClient
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.List
import scala.Some
import domain.Post
import dao.{TagDao, CommentDao, PostDao}
import scala.collection.JavaConversions._

object DomainEntityGenerator{

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

  def createComment(id: String, postId: String) = {
    createCommentWithoutId(postId).copy(id = Some(id))
  }

  def createCommentWithoutId(postId: String) = {
    Comment(postId = postId,
      body = "test comment body",
      authorName = "test name",
      authorPic = "http://some.thi.ng",
      authorUid = "test_UID"
    )
  }


  def main(args: Array[String]){
    implicit val ec = ExecutionContext.Implicits.global

      new PostDao().bucket.flush()
      new CommentDao().bucket.flush()
      new TagDao().bucket.flush()
  }
}
