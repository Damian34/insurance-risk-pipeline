package com.damian

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.damian.api.GeneratorController
import com.damian.messaging.InsuranceRecordProducer
import com.damian.service.{DatasetDownloader, InsuranceDataGenerator}
import com.typesafe.config.ConfigFactory

@main def run(): Unit = {
  given system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "InsuranceSystem")

  val config = ConfigFactory.load()
  val host = config.getString("server.host")
  val port = config.getInt("server.port")

  val generator = InsuranceDataGenerator(DatasetDownloader(), InsuranceRecordProducer())
  val controller = GeneratorController(generator)
  generator.startStream()

  Http().newServerAt(host, port).bind(controller.routes)
}

