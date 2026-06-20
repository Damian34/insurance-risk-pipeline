package com.damian.messaging

import com.damian.config.RabbitConfig
import com.damian.repository.InsuranceRecordRepository
import com.rabbitmq.client.GetResponse
import io.circe.generic.auto.*
import io.circe.parser.*
import org.slf4j.LoggerFactory

import java.util.concurrent.{Executors, TimeUnit}
import scala.collection.mutable.ListBuffer

class InsuranceRecordConsumer(rabbitConfig: RabbitConfig, repository: InsuranceRecordRepository) {
  private val log = LoggerFactory.getLogger(getClass)
  private val channel = rabbitConfig.createChannel()
  private val scheduler = Executors.newScheduledThreadPool(1)

  private val batchSize = 200
  private val pollInterval = 2 // seconds

  def start(): Unit = {
    scheduler.scheduleAtFixedRate(
      () => checkAndConsume(),
      0, pollInterval, TimeUnit.SECONDS
    )
    log.info("Consumer started, polling every {}s for batches of {}", pollInterval, batchSize)
  }

  private def checkAndConsume(): Unit = {
    val queueState = channel.queueDeclarePassive(QueueTopic.InsuranceRecordCreated)
    val available = queueState.getMessageCount

    if (available >= batchSize) {
      log.info(s"Found $available messages, fetching batch of $batchSize")
      val batch = fetchBatch(batchSize)
      processBatch(batch)
    } else {
      log.debug(s"Only $available messages available, waiting for more (need $batchSize)")
    }
  }

  private def fetchBatch(size: Int): List[(InsuranceRecordCreatedEvent, Long)] = {
    val results = ListBuffer[(InsuranceRecordCreatedEvent, Long)]()

    var i = 0
    var continue = true
    while (i < size && continue) {
      Option(channel.basicGet(QueueTopic.InsuranceRecordCreated, false)) match {
        case Some(response) =>
          decodeResponse(response).foreach(r => results += r)
          i += 1
        case None =>
          continue = false
      }
    }
    results.toList
  }

  private def decodeResponse(response: GetResponse): Option[(InsuranceRecordCreatedEvent, Long)] = {
    val message = String(response.getBody, "UTF-8")
    val deliveryTag = response.getEnvelope.getDeliveryTag

    decode[InsuranceRecordCreatedEvent](message) match {
      case Right(event) =>
        Some((event, deliveryTag))
      case Left(error) =>
        log.error(s"Failed to decode message: $error")
        channel.basicNack(deliveryTag, false, false)
        None
    }
  }

  private def processBatch(batch: List[(InsuranceRecordCreatedEvent, Long)]): Unit = {
    try {
      repository.save(batch.map(_._1))

      acknowledgeAll(batch)
      log.info(s"Batch of ${batch.size} records saved and acknowledged")
    } catch {
      case e: Exception => log.error(s"Failed to save batch: ${e.getMessage}")
    }
  }

  private def acknowledgeAll(batch: List[(InsuranceRecordCreatedEvent, Long)]): Unit = {
    val lastDeliveryTag = batch.map((_, tag) => tag).max
    channel.basicAck(lastDeliveryTag, true)
  }
}