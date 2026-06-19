package com.damian.service

import com.damian.config.properties.DatabaseProperties
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.slf4j.LoggerFactory

import java.sql.DriverManager

class RiskStatisticsJob(dbProperties: DatabaseProperties, calculator: StatisticsCalculator) {
  private val log = LoggerFactory.getLogger(getClass)
  
  def execute(spark: SparkSession): Unit = {
    val records: DataFrame = spark.read.jdbc(
      dbProperties.jdbcUrl,
      "insurance_records",
      dbProperties.connectionProperties
    )

    val prepared = records
      .withColumn("segment_value", calculator.concatAgeBucket())
      .withColumn("calculation_month", calculator.calculationMonth())

    val ageStats = calculator.calculate(prepared)
      .withColumn("segment_type", lit("AGE_BUCKET"))

    log.info("=== END AGE_BUCKET Processing ===")

    val regionPrepared = records
      .withColumn("segment_value", col("incident_state"))
      .withColumn("calculation_month", calculator.calculationMonth())

    val regionStats = calculator.calculate(regionPrepared)
      .withColumn("segment_type", lit("REGION"))

    log.info("=== END REGION Processing ===")

    val result = ageStats.unionByName(regionStats)
    upsertStatisticsToDatabase(result)
  }

  private def stagingStatistics(dataFrame: DataFrame): Unit = {
    val stagingTable = "risk_statistics_staging"
    dataFrame.write
      .mode("overwrite")
      .jdbc(
        dbProperties.jdbcUrl,
        stagingTable,
        dbProperties.connectionProperties
      )
  }

  private def upsertStatisticsToDatabase(dataFrame: DataFrame): Unit = {
    stagingStatistics(dataFrame)
    val sql =
      """
        |INSERT INTO risk_statistics (
        |    segment_type,
        |    calculation_month,
        |    segment_value,
        |    sample_size,
        |    avg_claim_cost,
        |    avg_total_claim_cost,
        |    avg_policy_premium,
        |    median_claim_cost,
        |    median_total_claim_cost,
        |    loss_ratio,
        |    fraud_rate,
        |    risk_index
        |)
        |SELECT
        |    segment_type,
        |    calculation_month,
        |    segment_value,
        |    sample_size,
        |    avg_claim_cost,
        |    avg_total_claim_cost,
        |    avg_policy_premium,
        |    median_claim_cost,
        |    median_total_claim_cost,
        |    loss_ratio,
        |    fraud_rate,
        |    risk_index
        |FROM risk_statistics_staging
        |ON CONFLICT (
        |    segment_type,
        |    calculation_month,
        |    segment_value
        |)
        |DO UPDATE SET
        |    sample_size = EXCLUDED.sample_size,
        |    avg_claim_cost = EXCLUDED.avg_claim_cost,
        |    avg_total_claim_cost = EXCLUDED.avg_total_claim_cost,
        |    avg_policy_premium = EXCLUDED.avg_policy_premium,
        |    median_claim_cost = EXCLUDED.median_claim_cost,
        |    median_total_claim_cost = EXCLUDED.median_total_claim_cost,
        |    loss_ratio = EXCLUDED.loss_ratio,
        |    fraud_rate = EXCLUDED.fraud_rate,
        |    risk_index = EXCLUDED.risk_index;
        |
        |DROP TABLE risk_statistics_staging;
        |""".stripMargin

    val conn = DriverManager.getConnection(
      dbProperties.jdbcUrl,
      dbProperties.username,
      dbProperties.password
    )

    try {
      conn.createStatement().execute(sql)
    } finally {
      conn.close()
    }
  }
}
