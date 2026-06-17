package com.damian.migration

import com.damian.config.properties.DatabaseProperties
import org.flywaydb.core.Flyway

class DatabaseMigration(properties: DatabaseProperties) {
  def migrate(): Unit = {
    val flyway = Flyway.configure()
      .dataSource(properties.jdbcUrl, properties.username, properties.password)
      .load()
    flyway.migrate()
  }
}
