package com.damian.config.properties

import com.typesafe.config.Config

case class SparkProperties(config: Config) {
  val url: String = if (config.hasPath("spark.url")) {
    s"spark://${config.getString("spark.url")}" // "spark://spark-master:7077"
  } else "local[*]"
  val schedulerEnable: Boolean = config.getString("spark.scheduler.enable").toBoolean
  val schedulerInterval: Long = config.getString("spark.scheduler.interval").toLong
}
