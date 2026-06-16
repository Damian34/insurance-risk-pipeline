package com.damian

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.damian.api.GeneratorController
import com.damian.messaging.InsuranceRecordProducer
import com.damian.service.{DatasetDownloader, InsuranceDataGenerator}

import scala.concurrent.ExecutionContext

@main def run(): Unit = {
  given system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "InsuranceSystem")
  given ExecutionContext = system.executionContext

  val generator = InsuranceDataGenerator(DatasetDownloader(), InsuranceRecordProducer())
  val controller = GeneratorController(generator)
  generator.startStream()

  //TODO: to change, server address should be modifiable by ENV
  Http().newServerAt("0.0.0.0", 8080).bind(controller.routes)
}

