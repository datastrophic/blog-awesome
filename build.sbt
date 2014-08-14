name := """myawesomeblog"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "ReactiveCouchbase repository" at "https://raw.github.com/ReactiveCouchbase/repository/master/snapshots"
)

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
    "org.webjars" % "underscorejs" % "1.6.0-3",
    "org.webjars" % "jquery" % "2.1.1",
    "org.webjars" % "html5shiv" % "3.7.2",
    "org.webjars" % "respond" % "1.4.2",
    "org.webjars" % "font-awesome" % "4.1.0" exclude("org.webjars", "bootstrap"),
    "org.webjars" % "bootswatch-flatly" % "3.2.0",
    "org.webjars" % "angularjs" % "1.2.18"
)