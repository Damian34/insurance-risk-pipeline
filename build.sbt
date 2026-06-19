ThisBuild / scalaVersion := "3.8.4"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.damian"
ThisBuild / description := "Insurance Risk Pipeline"

ThisBuild / assembly / assemblyMergeStrategy := {
  case PathList("module-info.class")                             => MergeStrategy.discard
  case PathList("META-INF", "versions", _, "module-info.class") => MergeStrategy.discard
  case PathList("META-INF", "io.netty.versions.properties")     => MergeStrategy.first
  case x =>
    val old = (assembly / assemblyMergeStrategy).value
    old(x)
}

lazy val root = (project in file("."))
  .aggregate(shared, generator, processingApi, sparkJob)

lazy val shared = (project in file("shared"))
  .settings(
    name := "shared",
    assembly / skip := true,
    libraryDependencies ++= Seq(
      "com.rabbitmq" % "amqp-client" % "5.32.0",
      "com.typesafe" % "config"      % "1.4.9"
    )
  )

lazy val generator = (project in file("data-generator"))
  .dependsOn(shared)
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "data-generator",
    assembly / mainClass    := Some("com.damian.run"),
    assembly / assemblyJarName := "app.jar",
    libraryDependencies ++= Seq(
      "org.slf4j"      % "slf4j-api"       % "2.0.18",
      "ch.qos.logback" % "logback-classic" % "1.5.34",
      "com.github.tototoshi" %% "scala-csv" % "2.0.0",
      "com.typesafe.akka" %% "akka-http"   % "10.5.3",
      "com.typesafe.akka" %% "akka-stream" % "2.8.8",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.8.8",
      "io.circe" %% "circe-core"    % "0.14.15",
      "io.circe" %% "circe-generic" % "0.14.15"
    )
  )

lazy val processingApi = (project in file("processing/api"))
  .dependsOn(shared)
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "processing-api",
    assembly / mainClass    := Some("com.damian.run"),
    assembly / assemblyJarName := "app.jar",
    libraryDependencies ++= Seq(
      "org.slf4j"      % "slf4j-api"       % "2.0.18",
      "ch.qos.logback" % "logback-classic" % "1.5.34",
      "io.circe" %% "circe-core"    % "0.14.15",
      "io.circe" %% "circe-generic" % "0.14.15",
      "io.circe" %% "circe-parser" % "0.14.15",
      "org.flywaydb" % "flyway-core" % "12.9.0",
      "org.flywaydb" % "flyway-database-postgresql" % "12.9.0",
      "org.postgresql" % "postgresql" % "42.7.11",
      "com.zaxxer" % "HikariCP" % "7.1.0"
    )
  )

lazy val sparkJob = (project in file("processing/spark-job"))
  .enablePlugins(AssemblyPlugin)
  .settings(
    scalaVersion := "2.12.18",
    name := "spark-job",
    assembly / mainClass    := Some("com.damian.JobApplication"),
    assembly / assemblyJarName := "app.jar",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config"      % "1.4.9",
      "org.slf4j"      % "slf4j-api"       % "2.0.18",
      "ch.qos.logback" % "logback-classic" % "1.5.34",
      "org.postgresql" % "postgresql" % "42.7.11",
      // spark to docker with cluster run
      "org.apache.spark" %% "spark-core" % "3.5.0" % "provided",
      "org.apache.spark" %% "spark-sql"  % "3.5.0" % "provided",
      // spark to local run
//      "org.apache.spark" %% "spark-core" % "3.5.0",
//      "org.apache.spark" %% "spark-sql" % "3.5.0",
    )
  )