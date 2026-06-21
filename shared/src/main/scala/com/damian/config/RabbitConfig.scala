package com.damian.config

import com.damian.config.properties.RabbitProperties
import com.damian.messaging.QueueTopic
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}

import scala.util.Using

class RabbitConfig(properties: RabbitProperties) {
  private val factory = ConnectionFactory()
  factory.setHost(properties.host)
  factory.setPort(properties.port)
  factory.setUsername(properties.userName)
  factory.setPassword(properties.password)

  private val connection: Connection = factory.newConnection()
  declareQueues()

  def createChannel(): Channel = connection.createChannel()

  private def declareQueues(): Unit = {
    Using(connection.createChannel()) { adminChannel =>
      adminChannel.queueDeclare(QueueTopic.InsuranceRecordCreated, true, false, false, null)
    }
  }

}
