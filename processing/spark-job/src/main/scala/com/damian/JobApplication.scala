package com.damian

import com.damian.config.properties.{DatabaseProperties, MinioProperties, SparkProperties}
import com.damian.migration.DatabaseMigration
import org.apache.spark.sql.SparkSession
import com.typesafe.config.ConfigFactory
import com.damian.service.{RiskStatisticsJob, StatisticsCalculator}
import org.slf4j.LoggerFactory
import java.util.concurrent.{Executors, Semaphore, TimeUnit}

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
  private val scheduler = Executors.newSingleThreadScheduledExecutor()
  private val jobSemaphore = new Semaphore(1)
  private val config = ConfigFactory.load()
  private val sparkProperties = SparkProperties(config)
  private val dbProperties = DatabaseProperties(config)
  private val minioProperties = MinioProperties(config)

  def main(args: Array[String]): Unit = {
    val dbMigration = new DatabaseMigration(dbProperties)
    dbMigration.migrate()
    val schedulingEnabled: Boolean = sparkProperties.schedulerEnable
    val interval: Long = sparkProperties.schedulerInterval
    val spark = SparkSession.builder()
      .appName("InsuranceJob")
      .master(sparkProperties.url)
      //.master("local[*]")
      //.master("spark://spark-master:7077")
      // Delta Lake
      .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
      .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
      // MinIO / S3A
      .config("spark.hadoop.fs.s3a.endpoint", minioProperties.endpoint)
      .config("spark.hadoop.fs.s3a.path.style.access", "true")
      .config("spark.hadoop.fs.s3a.access.key", minioProperties.accessKey)
      .config("spark.hadoop.fs.s3a.secret.key", minioProperties.secretKey)
      .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
      .config("spark.hadoop.fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider")
      .getOrCreate()
    //spark.sparkContext.setLogLevel("ERROR")

    if (schedulingEnabled) {
      log.info(s"Scheduler enabled. Running every $interval minutes")
      scheduler.scheduleAtFixedRate(
        () => safeExecute(spark),
        0, interval, TimeUnit.MINUTES
      )
    } else {
      log.info("Scheduler disabled. Running job once")
      safeExecute(spark)
    }
    spark.stop()
  }

  private def safeExecute(spark: SparkSession): Unit = {
    if (!jobSemaphore.tryAcquire()) {
      log.warn("Job is already running - skipping this execution")
      return
    }

    try {
      log.info("=== JOB START ===")
      execute(spark)
      log.info("=== JOB END ===")
    } catch {
      case e: Exception => log.error("Job failed", e)
    } finally {
      jobSemaphore.release()
    }
  }

  private def execute(spark: SparkSession): Unit = {
    val calculator = new StatisticsCalculator()
    val job = new RiskStatisticsJob(
      dbProperties,
      calculator,
      rawDataPath = s"s3a://${minioProperties.bucket}"
    )
    job.execute(spark)
  }
}
