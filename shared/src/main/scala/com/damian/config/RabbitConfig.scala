package com.damian.config

import com.damian.messaging.QueueTopic
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}
import com.typesafe.config.Config

import scala.util.Using

class RabbitConfig(config: Config) {
  private val factory = ConnectionFactory()
  factory.setHost(config.getString("rabbitmq.host"))
  factory.setPort(config.getInt("rabbitmq.port"))
  factory.setUsername(config.getString("rabbitmq.username"))
  factory.setPassword(config.getString("rabbitmq.password"))

  private val connection: Connection = factory.newConnection()
  declareQueues()

  def createChannel(): Channel = connection.createChannel()

  private def declareQueues(): Unit = {
    Using(connection.createChannel()) { adminChannel =>
      adminChannel.queueDeclare(QueueTopic.InsuranceRecordCreated, true, false, false, null)
    }
  }

}
