package com.damian.config.properties

import com.typesafe.config.Config

case class MinioProperties(config: Config)  {
  val endpoint: String = config.getString("minio.endpoint")
  val accessKey: String = config.getString("minio.access-key")
  val secretKey: String = config.getString("minio.secret-key")
  val bucket: String = config.getString("minio.bucket")
}
