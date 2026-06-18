package com.damian.repository

import com.damian.config.properties.DatabaseProperties
import com.damian.model.InsuranceRecord

import scala.util.Using
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

class InsuranceRecordRepository(properties: DatabaseProperties) {

  private val dataSource: HikariDataSource =
    val config = HikariConfig()
    config.setJdbcUrl(properties.jdbcUrl)
    config.setUsername(properties.username)
    config.setPassword(properties.password)
    config.setMaximumPoolSize(4)
    HikariDataSource(config)

  def save(record: InsuranceRecord): Unit =
    Using(dataSource.getConnection){ conn =>
      val stmt = conn.prepareStatement("""
        INSERT INTO insurance_records (
          policy_id, policy_state, policy_deductible, policy_annual_premium,
          insured_age, insured_sex, insured_education_level, insured_occupation,
          insured_hobbies, incident_date, incident_type, collision_type,
          incident_severity, authorities_contacted, incident_state, incident_city,
          incident_hour_of_the_day, number_of_vehicles_involved, bodily_injuries,
          witnesses, police_report_available, claim_amount, total_claim_amount,
          fraud_reported
        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        ON CONFLICT (policy_id) DO UPDATE SET
          policy_state              = EXCLUDED.policy_state,
          policy_deductible         = EXCLUDED.policy_deductible,
          policy_annual_premium     = EXCLUDED.policy_annual_premium,
          insured_age               = EXCLUDED.insured_age,
          insured_sex               = EXCLUDED.insured_sex,
          insured_education_level   = EXCLUDED.insured_education_level,
          insured_occupation        = EXCLUDED.insured_occupation,
          insured_hobbies           = EXCLUDED.insured_hobbies,
          incident_date             = EXCLUDED.incident_date,
          incident_type             = EXCLUDED.incident_type,
          collision_type            = EXCLUDED.collision_type,
          incident_severity         = EXCLUDED.incident_severity,
          authorities_contacted     = EXCLUDED.authorities_contacted,
          incident_state            = EXCLUDED.incident_state,
          incident_city             = EXCLUDED.incident_city,
          incident_hour_of_the_day  = EXCLUDED.incident_hour_of_the_day,
          number_of_vehicles_involved = EXCLUDED.number_of_vehicles_involved,
          bodily_injuries           = EXCLUDED.bodily_injuries,
          witnesses                 = EXCLUDED.witnesses,
          police_report_available   = EXCLUDED.police_report_available,
          claim_amount              = EXCLUDED.claim_amount,
          total_claim_amount        = EXCLUDED.total_claim_amount,
          fraud_reported            = EXCLUDED.fraud_reported,
          updated_at                = NOW()
      """)

      stmt.setString(1, record.policyId)
      stmt.setString(2, record.policyState)
      stmt.setInt(3, record.policyDeductible)
      stmt.setDouble(4, record.policyAnnualPremium)
      stmt.setInt(5, record.insuredAge)
      stmt.setString(6, record.insuredSex)
      stmt.setString(7, record.insuredEducationLevel)
      stmt.setString(8, record.insuredOccupation)
      stmt.setString(9, record.insuredHobbies)
      stmt.setObject(10, record.incidentDate)
      stmt.setString(11, record.incidentType)
      stmt.setString(12, record.collisionType)
      stmt.setString(13, record.incidentSeverity)
      stmt.setString(14, record.authoritiesContacted)
      stmt.setString(15, record.incidentState)
      stmt.setString(16, record.incidentCity)
      stmt.setInt(17, record.incidentHourOfTheDay)
      stmt.setInt(18, record.numberOfVehiclesInvolved)
      stmt.setInt(19, record.bodilyInjuries)
      stmt.setInt(20, record.witnesses)
      stmt.setBoolean(21, record.policeReportAvailable)
      stmt.setDouble(22, record.claimAmount)
      stmt.setDouble(23, record.totalClaimAmount)
      stmt.setBoolean(24, record.fraudReported)
      stmt.executeUpdate()
    }.get
}
