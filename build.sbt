ThisBuild / organization := "dedkot"
ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.5"

val AkkaVersion = "2.6.14"
lazy val akkaStream = (project in file("akka-stream"))
  .settings(
    name := "akka-stream",
    libraryDependencies ++= Seq(
      "com.lightbend.akka" %% "akka-stream-alpakka-file" % "2.0.2",
      "com.lightbend.akka" %% "akka-stream-alpakka-csv"  % "2.0.2",
      "com.typesafe.akka"  %% "akka-stream"              % AkkaVersion,
      "com.typesafe.akka"  %% "akka-stream-testkit"      % AkkaVersion % Test
    )
  )

val FlinkVersion = "1.13.0"
lazy val flinkStream = (project in file("flink-stream"))
  .settings(
    name := "flink-stream",
    scalaVersion := "2.12.13",
    libraryDependencies ++= Seq(
      "org.apache.flink" %% "flink-clients"            % FlinkVersion,
      "org.apache.flink" %% "flink-streaming-scala"    % FlinkVersion,
      "org.apache.flink" %% "flink-scala"              % FlinkVersion,
      "org.apache.flink" % "flink-metrics-dropwizard"  % FlinkVersion,
      "org.apache.flink" %% "flink-runtime-web"        % FlinkVersion,
      "org.apache.flink" %% "flink-metrics-prometheus" % FlinkVersion
    )
  )

val SparkVersion  = "2.4.5"
val doobieVersion = "0.9.0"
lazy val sparkStream = (project in file("spark-stream"))
  .settings(
    name := "spark-stream",
    scalaVersion := "2.12.10",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core"      % SparkVersion,
      "org.apache.spark" %% "spark-sql"       % SparkVersion,
      "org.tpolecat"     %% "doobie-core"     % doobieVersion,
      "org.tpolecat"     %% "doobie-postgres" % doobieVersion,
      "org.tpolecat"     %% "doobie-specs2"   % doobieVersion
    )
  )
