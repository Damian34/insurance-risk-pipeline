package com.damian.config.properties

import com.typesafe.config.Config

class RabbitProperties(config: Config) {
  val host: String = config.getString("rabbitmq.host")
  val port: Int = config.getInt("rabbitmq.port")
  val userName: String = config.getString("rabbitmq.username")
  val password: String = config.getString("rabbitmq.password")
}
