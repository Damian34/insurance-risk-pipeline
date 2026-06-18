package com.damian

import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._

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
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("InsuranceWorker")
      .master("local[*]")
      //.master("spark://spark-master:7077")
      .getOrCreate()

    println("=== START ===")
    println(s"Spark version: ${spark.version}")

    val df = createData(spark)

    println("=== INPUT ===")
    df.show(false)
    df.printSchema()

    val processed = process(df)

    println("=== AFTER PROCESS ===")
    processed.show(false)

    println("Partitions: " + processed.rdd.getNumPartitions)

    spark.stop()
  }

  def createData(spark: SparkSession): DataFrame = {
    import spark.implicits._

    Seq(
      (1, "Anna"),
      (2, "Bartek"),
      (4, "Damian")
    ).toDF("id", "name")
  }

  def process(df: DataFrame): DataFrame = {
    df.repartition(2)
      .withColumn("name_upper", upper(col("name")))
      .withColumn("is_even", col("id") % 2 === 0)
  }
}