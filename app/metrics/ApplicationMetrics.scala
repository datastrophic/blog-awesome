package metrics

import com.codahale.metrics.{JmxReporter, MetricRegistry}
import com.codahale.metrics.health.HealthCheckRegistry

object ApplicationMetrics {

  val metricRegistry = new MetricRegistry

  //http
  val httpRequests = metricRegistry.meter("fo.blog.http.requests")

  //couchbase
  val couchbaseReadRequests = metricRegistry.meter("fo.blog.couchbase.read.requests")
  val couchbaseWriteRequests = metricRegistry.meter("fo.blog.couchbase.write.requests")
  val couchbaseMRRequests = metricRegistry.meter("fo.blog.couchbase.mr.requests")

  val couchbaseReadTime = metricRegistry.timer("fo.blog.couchbase.read.time")
  val couchbaseWriteTime = metricRegistry.timer("fo.blog.couchbase.write.time")
  val couchbaseMRTime = metricRegistry.timer("fo.blog.couchbase.MR.time")

  //logic
  val postListReadTime = metricRegistry.timer("fo.blog.posts.list.generation.time")
  val singlePostReadTime = metricRegistry.timer("fo.blog.post.single.read.time")

}
