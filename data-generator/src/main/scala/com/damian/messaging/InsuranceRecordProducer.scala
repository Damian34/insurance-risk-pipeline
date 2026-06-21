package com.damian.messaging

import com.damian.config.RabbitConfig
import com.damian.model.InsuranceRecord
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.slf4j.LoggerFactory

class InsuranceRecordProducer(rabbitConfig: RabbitConfig) {
  private val log = LoggerFactory.getLogger(getClass)
  private val channel = rabbitConfig.createChannel()

  def produce(record: InsuranceRecord): Unit = {
    val event = InsuranceRecordCreatedEvent.of(record)
    val json = event.asJson.noSpaces
    channel.basicPublish("", QueueTopic.InsuranceRecordCreated, RabbitMessageProperties.basicProperties, json.getBytes("UTF-8"))
    log.info(s"Published event: ${event.eventId} for policy: ${record.policyId}")
  }
}
