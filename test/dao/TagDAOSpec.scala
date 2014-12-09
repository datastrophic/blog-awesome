package dao

import db.TagBucketClient
import scala.concurrent.Await
import scala.concurrent.duration._
import util.SpringContextHelper
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, Matchers, FunSpec}

@RunWith(classOf[JUnitRunner])
class TagDaoSpec extends FunSpec with TagBucketClient with Matchers with BeforeAndAfterAll with BeforeAndAfterEach{

  val context = SpringContextHelper.springContext
  private val tagDao: TagDao = context.getBean(classOf[TagDao])

  describe("Tag DAO"){

    it("correctly get tags from DB"){

      val tagsSample = List("tag1", "tag2", "tag3")

      Await.result(executeWithBucket(bucket => bucket.set[List[String]](tagDao.TagKey, tagsSample)), 5 seconds)

      val tags = Await.result(tagDao.getTags, 5 seconds)

      tags.size shouldEqual tagsSample.size
      tags.head shouldEqual tagsSample.head
      tags.last shouldEqual tagsSample.last

      Await.result(executeWithBucket(bucket => bucket.delete(tagDao.TagKey)), 5 seconds)

      val emptyResult = Await.result(tagDao.getTags, 5 seconds)

      emptyResult.size shouldEqual 0
    }

    it("correctly merge tags"){
      val tagsSample = List("tag1", "tag2")

      Await.result(executeWithBucket(bucket => bucket.set[List[String]](tagDao.TagKey, tagsSample)), 5 seconds)

      val tagsToMerge = List("tag2", "tag3")

      Await.result(tagDao.mergeTags(tagsToMerge), 5 seconds)

      val mergedTags = Await.result(executeWithBucket(bucket => bucket.get[List[String]](tagDao.TagKey)), 5 seconds)

      mergedTags.isDefined shouldEqual true
      mergedTags.get.size shouldEqual tagsSample.size + 1
      mergedTags.get.head shouldEqual tagsSample.head
      mergedTags.get.last shouldEqual tagsToMerge.last

      Await.result(executeWithBucket(bucket => bucket.delete(tagDao.TagKey)), 5 seconds)

      val emptyResult = Await.result(tagDao.getTags, 5 seconds)

      emptyResult.size shouldEqual 0
    }
  }

  override protected def afterAll(): Unit = {
    super.afterAll()

      executeWithBucket(bucket => bucket.delete(tagDao.TagKey))

    context.close()
    context.destroy()
  }
}
