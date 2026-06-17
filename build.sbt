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
  .aggregate(shared, generator, processingApi)

lazy val shared = (project in file("shared"))
  .settings(
    name := "shared",
    assembly / skip := true,
    libraryDependencies ++= Seq(
      "com.rabbitmq" % "amqp-client" % "5.31.0",
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
      "ch.qos.logback" % "logback-classic" % "1.5.34"
    )
  )
