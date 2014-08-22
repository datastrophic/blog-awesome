package dao

import db.ReactiveCouchbaseClient
import scala.concurrent.Future
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by akirillov on 8/22/14.
 */
trait InMemoryDAO[T]{

  private val store = new ConcurrentHashMap[String, T]

  def save(key: String, entity: T) = {
    store.put(key, entity)
  }

  def get(key: String): Future[Option[T]] = {
    Future{
      if(store.containsKey(key)){
        Some(store.get(key))
      } else {
        None
      }
    }
  }

  def delete(key: String) = {
    store.remove(key)
  }

}