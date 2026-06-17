package com.damian.messaging

import com.damian.config.RabbitConfig
import com.damian.repository.InsuranceRecordRepository
import com.rabbitmq.client.{CancelCallback, DeliverCallback, Delivery}
import io.circe.generic.auto.*
import io.circe.parser.*
import org.slf4j.LoggerFactory

class InsuranceRecordConsumer(rabbitConfig: RabbitConfig, repository: InsuranceRecordRepository) {
  private val log = LoggerFactory.getLogger(getClass)
  private val channel = rabbitConfig.createChannel()

  def start(): Unit = {
    val deliverCallback = InsuranceRecordDeliverCallback()
    val cancelCallback: CancelCallback = _ => log.warn("Consumer cancelled")

    channel.basicConsume(QueueTopic.InsuranceRecordCreated, true, deliverCallback, cancelCallback)
    log.info("Consumer started, waiting for messages...")
  }

  private class InsuranceRecordDeliverCallback extends DeliverCallback {
    override def handle(consumerTag: String, delivery: Delivery): Unit =
      val message = String(delivery.getBody, "UTF-8")
      decode[InsuranceRecordCreatedEvent](message)
        .fold(
          error => log.error(s"Failed to decode message: $error"),
          event => process(event)
        )
  }

  private def process(event: InsuranceRecordCreatedEvent): Unit = {
    log.info(s"Consumed event: ${event.eventId} for policy: ${event.record.policyId}")
    repository.save(event.record)
  }
}
