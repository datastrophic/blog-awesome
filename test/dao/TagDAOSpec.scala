package dao

import org.specs2.mutable._
import db.ReactiveCouchbaseClient
import scala.concurrent.Await
import scala.concurrent.duration._
import org.specs2.time.NoTimeConversions

class TagDAOSpec extends Specification with ReactiveCouchbaseClient with NoTimeConversions{

  sequential

  "TagDAO" should {

    "correctly get tags from DB" in {

      val tagsSample = List("tag1", "tag2", "tag3")

      Await.result(executeWithBucket(bucket => bucket.set[List[String]](TagDao.TagKey, tagsSample)), 5 seconds)

      TagDao.getTags
      val tags = Await.result(TagDao.getTags, 5 seconds)

      tags.size mustEqual tagsSample.size
      tags.head mustEqual tagsSample.head
      tags.last mustEqual tagsSample.last

      Await.result(executeWithBucket(bucket => bucket.delete(TagDao.TagKey)), 5 seconds)

      val emptyResult = Await.result(TagDao.getTags, 5 seconds)

      emptyResult.size mustEqual 0
    }

    "correctly merge tags" in {
      val tagsSample = List("tag1", "tag2")

      Await.result(executeWithBucket(bucket => bucket.set[List[String]](TagDao.TagKey, tagsSample)), 5 seconds)

      val tagsToMerge = List("tag2", "tag3")

      Await.result(TagDao.mergeTags(tagsToMerge), 5 seconds)

      val mergedTags = Await.result(executeWithBucket(bucket => bucket.get[List[String]](TagDao.TagKey)), 5 seconds)

      mergedTags.isDefined mustEqual true
      mergedTags.get.size mustEqual tagsSample.size + 1
      mergedTags.get.head mustEqual tagsSample.head
      mergedTags.get.last mustEqual tagsToMerge.last

      Await.result(executeWithBucket(bucket => bucket.delete(TagDao.TagKey)), 5 seconds)

      val emptyResult = Await.result(TagDao.getTags, 5 seconds)

      emptyResult.size mustEqual 0
    }
  }
}
