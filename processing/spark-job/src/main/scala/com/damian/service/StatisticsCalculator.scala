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
        // average severity score
        avg(when(col("incident_severity") === "Total Loss", 1).otherwise(0)).as("avg_severity_score"),
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
    val lossNorm = least(col("loss_ratio"), lit(2.0)) / lit(2.0)
    lossNorm * lit(0.5) +
      col("fraud_rate") * lit(0.3) +
      col("avg_severity_score") * lit(0.2)
  }

  def concatAgeBucket(): Column = {
    val age = col("insured_age")

    when(age.between(18, 24), "18-24")
      .when(age.between(25, 34), "25-34")
      .when(age.between(35, 44), "35-44")
      .when(age.between(45, 54), "45-54")
      .when(age.between(55, 64), "55-64")
      .otherwise("65+")
  }

  def calculationMonth(): Column = date_format(col("incident_date"), "yyyy-MM")
}
