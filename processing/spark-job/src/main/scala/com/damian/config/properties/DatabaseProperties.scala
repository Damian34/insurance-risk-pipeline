package com.damian.config.properties

import com.typesafe.config.Config

import java.util.Properties

case class DatabaseProperties(config: Config) {
  val jdbcUrl: String = config.getString("datasource.jdbc")
  val username: String = config.getString("datasource.username")
  val password: String = config.getString("datasource.password")

  val connectionProperties = new Properties()
  connectionProperties.put("user", username)
  connectionProperties.put("password",password)
  connectionProperties.put("driver", "org.postgresql.Driver")
}
