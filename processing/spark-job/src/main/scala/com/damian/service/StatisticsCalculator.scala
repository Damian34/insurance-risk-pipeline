package com.damian.service

import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.functions._

class StatisticsCalculator {
  def calculate(df: DataFrame): DataFrame = {
    df
      .withColumn("is_fraud", when(col("fraud_reported") === "Y", 1).otherwise(0))
      .groupBy(col("calculation_month"), col("segment_value"))
      .agg(
        // number of all records in the segment
        count("*").as("sample_size"),
        // average claim amount
        avg("claim_amount").as("avg_claim_cost"),
        // average total estimated claim amount
        avg("total_claim_amount").as("avg_total_claim_cost"),
        // average annual insurance policy premium
        avg("policy_annual_premium").as("avg_policy_premium"),
        // median claim amount (approximate one)
        expr("percentile_approx(claim_amount, 0.5)").as("median_claim_cost"),
        // median total claim amount (approximate one)
        expr("percentile_approx(total_claim_amount, 0.5)").as("median_total_claim_cost"),
        // percentage of records marked as fraud
        avg("is_fraud").as("fraud_rate"),

        // for loss_ratio:
        // auxiliary sum used to calculate loss ratio
        sum("total_claim_amount").as("total_claims_sum"),
        // auxiliary sum used to calculate loss ratio
        sum("policy_annual_premium").as("total_premiums_sum")
      )
      // ratio of claims paid to premiums collected (simply: >1 = loss, <1 = profit)
      .withColumn("loss_ratio", col("total_claims_sum") / col("total_premiums_sum"))
      // a simplified risk indicator inspired by the weighted scoring methodology
      .withColumn("risk_index", calculateRiskIndex())
      .drop("total_claims_sum", "total_premiums_sum")
  }

  private def calculateRiskIndex(): Column = {
    val lossRatioNorm =
      least(col("avg_claim_cost") / col("avg_policy_premium"), lit(2.0)) / lit(2.0)
    val severity =
      col("median_total_claim_cost") / col("avg_claim_cost")
    lossRatioNorm * lit(0.5) +
      col("fraud_rate") * lit(0.3) +
      severity * lit(0.2)
  }

  def concatAgeBucket(): Column = {
      val step = 10
      concat(
        (floor(col("insured_age") / step) * step).cast("int").cast("string"),
        lit("_"),
        (floor(col("insured_age") / step) * step + step - 1).cast("int").cast("string")
      )
  }

  def calculationMonth(): Column = date_format(col("incident_date"), "yyyy-MM")
}
