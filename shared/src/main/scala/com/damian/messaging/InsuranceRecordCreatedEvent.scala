package com.damian.messaging

import com.damian.model.InsuranceRecord

import java.time.Instant
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
      Instant.now(),
      record
    )
