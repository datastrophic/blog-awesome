package dao

import scala.concurrent.Future
import org.reactivecouchbase.client.OpResult
import net.spy.memcached.ops.OperationStatus
import db.TagBucketClient

class TagDao extends BaseDao[List[String]]  with TagBucketClient{

  val TagKey = "fo::tags"

  //if key is not defined empty list is returned
  def getTags: Future[List[String]] = {
    get(TagKey).map(option => option.fold(List[String]())(identity[List[String]]))
  }

  def mergeTags(tags: List[String]): Future[OpResult] = {
    if(!tags.isEmpty) {
      get(TagKey) flatMap {
        case Some(savedTags) =>
          val mergedTagsSet = savedTags.toSet[String] ++ tags
          save(TagKey, mergedTagsSet.toList)
        case None => save(TagKey, tags)
      }
    } else Future(OpResult(new OperationStatus(true, "Input tag list is empty, no DB operations needed.")))
  }


}
