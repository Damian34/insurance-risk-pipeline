package com.damian.service

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.damian.messaging.InsuranceRecordProducer
import org.slf4j.LoggerFactory

import java.util.concurrent.Semaphore
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.util.Random
import scala.util.chaining.scalaUtilChainingOps

class InsuranceDataGenerator(
                              downloader: DatasetDownloader,
                              producer: InsuranceRecordProducer
                            )(using system: ActorSystem[Nothing]) {
  private val log = LoggerFactory.getLogger(getClass)
  private val streamSemaphore = Semaphore(1) // permit one propagation a time

  def startStream(): String =
    if streamSemaphore.tryAcquire() then
      stream()
      "Propagation start.".tap(log.info)
    else
      "Propagation already running!".tap(log.info)

  private def stream(): Unit =
    val reader = InsuranceDataReader(downloader)
    val streamFuture = Source.fromIterator(() => reader.records())
      // sleep between sends in order to simulate real system output
      .throttle(1, FiniteDuration(Random.between(50, 300), "ms"))
      .runWith(Sink.foreach(r => producer.produce(r)))
    streamFuture
      .andThen(_ => reader.close())
      .andThen(_ => streamSemaphore.release())
  
}
