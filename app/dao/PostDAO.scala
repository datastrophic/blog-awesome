package dao

import domain.Post
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by akirillov on 8/14/14.
 */
object PostDAO extends InMemoryDAO[Post]{

  def update(post: Post) = {
    get(post.id.get) map {newPost =>
      if(newPost.isDefined) {
        //merge logic
        save(post.id.get, newPost.get)
      }
    }
  }

  def exists(uid: String): Boolean = {
    Await.result(get(uid), 5 seconds).isDefined
  }
}
