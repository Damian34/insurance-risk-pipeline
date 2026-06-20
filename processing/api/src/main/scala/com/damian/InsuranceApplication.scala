package com.damian

import com.damian.config.RabbitConfig
import com.damian.config.properties.*
import com.damian.messaging.InsuranceRecordConsumer
import com.damian.repository.InsuranceRecordRepository
import com.typesafe.config.ConfigFactory

@main def run(): Unit = {
  val config = ConfigFactory.load()
  val rabbitConfig = RabbitConfig(properties=RabbitProperties(config))
  val minoProperties = MinioProperties(config)
  val recordRepository = InsuranceRecordRepository(minoProperties)
  val consumer = InsuranceRecordConsumer(rabbitConfig, recordRepository)
  consumer.start() // blocked
}
