package com.damian.service

import com.damian.model.InsuranceRecord
import com.github.tototoshi.csv.CSVReader

class InsuranceDataReader(downloader: DatasetDownloader) extends AutoCloseable {
  private val csvPath = downloader.downloadFile()
  private val reader = CSVReader.open(csvPath.toFile)

  def records(): Iterator[InsuranceRecord] = 
    reader
      .iteratorWithHeaders
      .map(row => InsuranceRecord.of(row))

  override def close(): Unit = reader.close()
}
