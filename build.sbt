ThisBuild / organization := "dedkot"
ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.5"

val AkkaVersion = "2.6.14"
lazy val akkaStream = (project in file("akka-stream"))
  .settings(
    libraryDependencies ++= Seq(
      "com.lightbend.akka" %% "akka-stream-alpakka-file" % "2.0.2",
      "com.lightbend.akka" %% "akka-stream-alpakka-csv"  % "2.0.2",
      "com.typesafe.akka"  %% "akka-stream"              % AkkaVersion,
      "com.typesafe.akka"  %% "akka-stream-testkit"      % AkkaVersion % Test
    )
  )
