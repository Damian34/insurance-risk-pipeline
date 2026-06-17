package com.damian.config.properties

import com.typesafe.config.Config

class DatabaseProperties(config: Config) {
  val jdbcUrl: String = config.getString("datasource.jdbc")
  val username: String = config.getString("datasource.username")
  val password: String = config.getString("datasource.password")
}
