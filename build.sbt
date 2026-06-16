ThisBuild / scalaVersion := "3.8.4"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.damian"
ThisBuild / description := "Insurance Risk Pipeline"

lazy val root = (project in file("."))
  .aggregate(generator)

lazy val generator = (project in file("insurance-data-generator"))
  .settings(
    name := "insurance-data-generator",
      libraryDependencies ++= Seq(
      "org.slf4j"      % "slf4j-api"       % "2.0.18",
      "ch.qos.logback" % "logback-classic" % "1.5.34"
    )
  )

