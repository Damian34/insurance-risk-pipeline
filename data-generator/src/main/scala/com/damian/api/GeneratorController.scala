package com.damian.api

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.damian.service.InsuranceDataGenerator

class GeneratorController(generator: InsuranceDataGenerator) {
  val routes: Route =
    path("start"):
      get:
        val msg = generator.startStream()
        complete(msg)
}

