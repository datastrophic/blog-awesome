import play.PlayScala

name := """myawesomeblog"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Classpaths.sbtPluginReleases,
  "ReactiveCouchbase repository" at "https://raw.github.com/ReactiveCouchbase/repository/master/snapshots"
)

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

javacOptions in Compile ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies ++= Seq(
    filters,
    cache,
    //Clients
    "org.reactivecouchbase" %% "reactivecouchbase-core" % "0.3-SNAPSHOT",
    //Auth
    "ws.securesocial" %% "securesocial" % "master-SNAPSHOT",
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
    "org.scalatestplus" %% "play" % "1.1.0"
)

instrumentSettings