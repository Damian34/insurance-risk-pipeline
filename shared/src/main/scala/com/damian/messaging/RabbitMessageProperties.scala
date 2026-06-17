package com.damian.messaging

import com.rabbitmq.client.AMQP

object RabbitMessageProperties:
  val basicProperties: AMQP.BasicProperties = AMQP.BasicProperties.Builder()
    .contentType("application/json")
    .deliveryMode(2)
    .build()
