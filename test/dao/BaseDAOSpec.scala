package dao

import org.specs2.mutable._
import db.ReactiveCouchbaseClient
import scala.concurrent.Await
import play.api.libs.json.Json
import scala.concurrent.duration._
import org.specs2.time.NoTimeConversions


/**
 * Created by akirillov on 9/16/14.
 */
class BaseDAOSpec extends Specification with ReactiveCouchbaseClient with NoTimeConversions{

  case class TestEntity(id: String, number: Int, long: Long)

  implicit val testEntityFormat = Json.format[TestEntity]

  object BaseDAOImpl extends BaseDao[TestEntity]


  val sampleEntity = TestEntity("uid", 1, 100L)

  "BaseDAO" should {

    "properly create, read and delete entity" in {
      val key = "save"

      Await.result(BaseDAOImpl.save(key, sampleEntity), 5 seconds)

      val expectedEntity = Await.result(executeWithBucket(bucket => bucket.get[TestEntity](key)), 5 seconds)

      expectedEntity mustNotEqual None

      val savedEntity = expectedEntity.get
      savedEntity.id mustEqual sampleEntity.id
      savedEntity.number mustEqual sampleEntity.number
      savedEntity.long mustEqual sampleEntity.long

      Await.result(executeWithBucket(bucket => bucket.delete(key)), 5 seconds)

      val emptyResult = Await.result(executeWithBucket(bucket => bucket.get[TestEntity](key)), 5 seconds)

      emptyResult mustEqual None
    }
  }
}
