package dao

import domain.Post
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, ExecutionContext, Await}
import scala.concurrent.duration._
import domain.DomainJsonFormats._
import org.reactivecouchbase.client.OpResult
import play.api.libs.json.{JsObject, Writes, Reads}
import com.couchbase.client.protocol.views.{Stale, ComplexKey, Query}

/**
 * Created by akirillov on 8/28/14.
 */
object TagDao extends BaseDao[List[String]]{

  private val TagKey = "fo::tags"

  def getTags = {
    get(TagKey)
  }

  def mergeTags(tags: List[String]) = {
    if(!tags.isEmpty) {

      get(TagKey) map {
        case Some(savedTags) =>
          val mergedTagsSet = savedTags.toSet[String] ++ tags
          save(TagKey, mergedTagsSet.toList)
        case None => save(TagKey, tags)
      }

    }
  }

}
