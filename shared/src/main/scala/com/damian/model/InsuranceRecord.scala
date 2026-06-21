package com.damian.model

import java.time.LocalDate

case class InsuranceRecord(
                            policyId: String,
                            policyState: String,
                            policyDeductible: Int,
                            policyAnnualPremium: Double,
                            insuredAge: Int,
                            insuredSex: String,
                            insuredEducationLevel: String,
                            insuredOccupation: String,
                            insuredHobbies: String,
                            incidentDate: LocalDate,
                            incidentType: String,
                            collisionType: String,
                            incidentSeverity: String,
                            authoritiesContacted: String,
                            incidentState: String,
                            incidentCity: String,
                            incidentHourOfTheDay: Int,
                            numberOfVehiclesInvolved: Int,
                            bodilyInjuries: Int,
                            witnesses: Int,
                            policeReportAvailable: Boolean,
                            claimAmount: Double,
                            totalClaimAmount: Double,
                            fraudReported: Boolean
                          )

object InsuranceRecord:
  def of(row: Map[String, String]): InsuranceRecord =
    InsuranceRecord(
      policyId                = row("policy_id"),
      policyState             = row("policy_state"),
      policyDeductible        = row("policy_deductible").toInt,
      policyAnnualPremium     = row("policy_annual_premium").toDouble,
      insuredAge              = row("insured_age").toInt,
      insuredSex              = row("insured_sex"),
      insuredEducationLevel   = row("insured_education_level"),
      insuredOccupation       = row("insured_occupation"),
      insuredHobbies          = row("insured_hobbies"),
      incidentDate            = LocalDate.parse(row("incident_date")),
      incidentType            = row("incident_type"),
      collisionType           = row("collision_type"),
      incidentSeverity        = row("incident_severity"),
      authoritiesContacted    = row("authorities_contacted"),
      incidentState           = row("incident_state"),
      incidentCity            = row("incident_city"),
      incidentHourOfTheDay    = row("incident_hour_of_the_day").toInt,
      numberOfVehiclesInvolved = row("number_of_vehicles_involved").toInt,
      bodilyInjuries          = row("bodily_injuries").toInt,
      witnesses               = row("witnesses").toInt,
      policeReportAvailable   = row("police_report_available") == "Yes",
      claimAmount             = row("claim_amount").toDouble,
      totalClaimAmount        = row("total_claim_amount").toDouble,
      fraudReported           = row("fraud_reported") == "Y"
    )

