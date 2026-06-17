package com.damian.service

import com.damian.exception.DatasetDownloadException
import org.slf4j.LoggerFactory

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.file.{Files, Path, Paths}
import java.util.zip.ZipInputStream
import scala.util.Using

class DatasetDownloader {
  /**
   * https://www.kaggle.com/datasets/ahluwaliasaksham/car-insurance-fraud-detection-dataset?utm_source=chatgpt.com
   */
  private val log = LoggerFactory.getLogger(getClass)
  private val DATA_SET = "ahluwaliasaksham/car-insurance-fraud-detection-dataset"
  private val DATA_SET_FILE_NAME = "car_insurance_fraud_dataset.csv"

  def downloadFile(): Path = {
    val cachePath = Paths.get(s"data/$DATA_SET_FILE_NAME")
    if Files.exists(cachePath) then
      log.info(s"Dataset file already exists, skipping download.")
      cachePath
    else
      log.info("Downloading dataset from Kaggle...")
      Files.createDirectories(cachePath.getParent)

      val client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build()

      val request = HttpRequest.newBuilder()
        .uri(URI.create(kaggleDownloadApiURL(DATA_SET)))
        .GET()
        .build()

      val zipPath = cachePath.getParent.resolve("archive.zip")
      val response = client.send(request, HttpResponse.BodyHandlers.ofFile(zipPath))

      if response.statusCode() == 200 then
        log.info("Dataset downloaded successfully.")
        unzipSingleFile(zipPath, cachePath)
        deleteFile(zipPath)
        cachePath
      else
        deleteFile(zipPath)
        throw new DatasetDownloadException(s"Failed to download Kaggle dataset (status ${response.statusCode()})")
  }

  private def deleteFile(path: Path): Unit = Files.deleteIfExists(path)

  private def unzipSingleFile(zipFile: Path, targetFile: Path): Unit = {
    val fileName = targetFile.getFileName.toString
    Using(ZipInputStream(Files.newInputStream(zipFile))): zis =>
      Iterator.continually(zis.getNextEntry)
        .takeWhile(_ != null)
        .find(_.getName == fileName)
        .foreach(_ => Files.copy(zis, targetFile))
    .get
  }

  private def kaggleDownloadApiURL(source: String): String =
    s"https://www.kaggle.com/api/v1/datasets/download/$source"

}
