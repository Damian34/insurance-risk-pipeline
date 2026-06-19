package com.damian

import com.damian.config.properties.DatabaseProperties
import org.apache.spark.sql.SparkSession
import com.typesafe.config.ConfigFactory
import com.damian.service.{RiskStatisticsJob, StatisticsCalculator}
import org.slf4j.LoggerFactory

/**
 * To local run set:
 * 1. in JobApplication set:
 * .master("local[*]")
 * 2. in build.sbt change to:
 * "org.apache.spark" %% "spark-core" % "3.5.0",
 * "org.apache.spark" %% "spark-sql" % "3.5.0"
 * 3. in IntelliJ run config add VM options:
 * --add-exports=java.base/sun.nio.ch=ALL-UNNAMED
 *
 * To docker run:
 * 1. in JobApplication set:
 * .master("spark://spark-master:7077")
 * 2. in build.sbt change to:
 * "org.apache.spark" %% "spark-core" % "3.5.0" % "provided",
 * "org.apache.spark" %% "spark-sql"  % "3.5.0" % "provided"
 * 3. Run spark-job in docker-compose.yaml
 */
object JobApplication {
  private val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("InsuranceJob")
      //.master("local[*]")
      .master("spark://spark-master:7077")
      .config("spark.sql.codegen.comments", false)
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    val config = ConfigFactory.load()
    val dbProperties = DatabaseProperties(config)
    val calculator = new StatisticsCalculator()
    val job = new RiskStatisticsJob(dbProperties, calculator)

    log.info("=== START ===")
    job.execute(spark)
    spark.stop()
    log.info("=== END ===")
  }
}
