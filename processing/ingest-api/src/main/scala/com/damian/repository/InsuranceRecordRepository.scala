package com.damian.repository

import com.damian.config.properties.MinioProperties
import com.damian.messaging.InsuranceRecordCreatedEvent
import com.damian.model.InsuranceRecord
import io.circe.Json
import io.circe.generic.auto.*
import io.circe.syntax.*
import io.minio.{MinioClient, PutObjectArgs}
import org.slf4j.LoggerFactory

import java.io.ByteArrayInputStream
import java.time.Instant
import java.util.UUID

class InsuranceRecordRepository(properties: MinioProperties) {
  private val log = LoggerFactory.getLogger(getClass)
  private val JsonSnakeCase = JsonSnakeCaseTransformer()

  private val client: MinioClient = MinioClient.builder()
    .endpoint(properties.endpoint)
    .credentials(properties.accessKey, properties.secretKey)
    .build()

  def save(batch: List[InsuranceRecordCreatedEvent]): Unit = {
    val fileName = generateFileName()

    val contentBytes = batch
      .map(event => withOccurredAt(event, event.occurredAt))
      .mkString("\n")
      .getBytes("UTF-8")

    client.putObject(
      PutObjectArgs.builder()
        .bucket(properties.bucket)
        .`object`(fileName)
        .stream(ByteArrayInputStream(contentBytes), contentBytes.length.toLong, -1)
        .contentType("application/x-ndjson")
        .build()
    )

    log.info(s"Saved batch of ${batch.size} records to $fileName")
  }

  /** append "occurred_at" field */
  private def withOccurredAt(event: InsuranceRecordCreatedEvent, occurredAt: Instant): String =
    val json = event.record.asJson
      .deepMerge(Json.obj("occurred_at" -> occurredAt.toString.asJson))
    JsonSnakeCase.transformKeys(json).noSpaces

  private def generateFileName(): String =
    s"insurance-records-${Instant.now()}-${UUID.randomUUID()}.json"
}
