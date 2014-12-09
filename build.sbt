import play.PlayScala

name := """blog-awesome"""

version := "1.0"

name in Universal := "blog-awesome"

scalaVersion := "2.11.2"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

parallelExecution in Test := false

fork in Test := false

logLevel := Level.Info

javacOptions in Compile ++= Seq("-source", "1.8", "-target", "1.8")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Classpaths.sbtPluginReleases,
  "ReactiveCouchbase" at "https://raw.github.com/ReactiveCouchbase/repository/master/releases/",
  "ReactiveCouchbase Snapshots" at "https://raw.github.com/ReactiveCouchbase/repository/master/snapshots/",
  "SpringSource Snapshot Repository" at "http://repo.springsource.org/snapshot"
)

libraryDependencies ++= Seq(
  filters,
  cache,
  //Clients
  "org.reactivecouchbase" %% "reactivecouchbase-core" % "0.4-SNAPSHOT",
  //Auth
  "ws.securesocial" %% "securesocial" % "master-SNAPSHOT",
  "org.springframework.scala" %% "spring-scala" % "1.0.0.BUILD-SNAPSHOT",
  "javax.inject" % "javax.inject" % "1",
  "org.springframework" % "spring-test" % "4.1.1.RELEASE",
  // WebJars (i.e. client-side) dependencies
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "requirejs" % "2.1.14-1",
  "org.webjars" % "underscorejs" % "1.6.0-3" exclude("org.webjars", "jquery"),
  "org.webjars" % "jquery" % "2.1.1",
  "org.webjars" % "html5shiv" % "3.7.2",
  "org.webjars" % "respond" % "1.4.2",
  "org.webjars" % "font-awesome" % "4.1.0" exclude("org.webjars", "bootstrap"),
  "org.webjars" % "bootswatch-flatly" % "3.2.0",
  "org.webjars" % "angularjs" % "1.2.18",
  "org.webjars" % "markdown-js" % "0.5.0-1",
  "org.webjars" % "bootbox" % "4.3.0",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3",
  "com.sksamuel.scrimage" %% "scrimage-core" % "1.3.20",
  "com.sksamuel.scrimage" %% "scrimage-filters" % "1.3.20",
  "org.scalatestplus" %% "play" % "1.1.0",
  "com.codahale.metrics" % "metrics-core" % "3.0.1",
  "com.codahale.metrics" % "metrics-healthchecks" % "3.0.1"
)

instrumentSettings