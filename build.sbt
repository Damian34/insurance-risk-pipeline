ThisBuild / scalaVersion := "3.8.4"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.damian"
ThisBuild / description := "Insurance Risk Pipeline"

lazy val root = (project in file("."))
  .aggregate(shared, generator)

lazy val shared = (project in file("insurance-shared"))
  .settings(
    name := "insurance-shared"
  )

lazy val generator = (project in file("insurance-data-generator"))
  .dependsOn(shared)
  .settings(
    name := "insurance-data-generator",
      libraryDependencies ++= Seq(
        "org.slf4j"      % "slf4j-api"       % "2.0.18",
        "ch.qos.logback" % "logback-classic" % "1.5.34",
        "com.github.tototoshi" %% "scala-csv" % "2.0.0",
        "com.typesafe.akka" %% "akka-http"   % "10.5.3",
        //"com.typesafe.akka" %% "akka-http-spray-json" % "10.5.3",
        "com.typesafe.akka" %% "akka-stream" % "2.8.8",
        "com.typesafe.akka" %% "akka-actor-typed" % "2.8.8",
        "com.rabbitmq" % "amqp-client" % "5.31.0",
        "io.circe" %% "circe-core"    % "0.14.15",
        "io.circe" %% "circe-generic" % "0.14.15"
    )
  )

