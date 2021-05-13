ThisBuild / organization := "dedkot"
ThisBuild / version      := "0.1"
ThisBuild / scalaVersion := "2.13.5"

val AkkaVersion = "2.6.14"
lazy val akkaStream = (project in file("akka-stream"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test
    )
  )
