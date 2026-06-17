package com.damian

import com.damian.config.RabbitConfig
import com.damian.config.properties.{DatabaseProperties, RabbitProperties}
import com.damian.messaging.InsuranceRecordConsumer
import com.damian.migration.DatabaseMigration
import com.damian.repository.InsuranceRecordRepository
import com.typesafe.config.ConfigFactory

@main def run(): Unit = {
  val config = ConfigFactory.load()
  val rabbitConfig = RabbitConfig(properties=RabbitProperties(config))
  val dbProperties = DatabaseProperties(config)
  val dbMigration = DatabaseMigration(dbProperties)
  dbMigration.migrate()
  val recordRepository = InsuranceRecordRepository(dbProperties)
  val consumer = InsuranceRecordConsumer(rabbitConfig, recordRepository)
  consumer.start()
  Thread.currentThread().join() // blokuje
}
