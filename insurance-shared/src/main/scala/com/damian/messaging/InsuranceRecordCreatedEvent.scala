package com.damian.messaging

import com.damian.model.InsuranceRecord

import java.time.{Instant, LocalDate, ZoneOffset}
import java.util.UUID

case class InsuranceRecordCreatedEvent (
                                         eventId: String,
                                           occurredAt: Instant,
                                         record: InsuranceRecord
)

object InsuranceRecordCreatedEvent:
  def of(record: InsuranceRecord): InsuranceRecordCreatedEvent =
    InsuranceRecordCreatedEvent(
      UUID.randomUUID().toString,
      occurredAt(record),
      record
    )

  private def occurredAt(record: InsuranceRecord): Instant =
    LocalDate.parse(record.incidentDate).atStartOfDay().toInstant(ZoneOffset.UTC)
