package com.damian.messaging

import com.damian.model.InsuranceRecord

class InsuranceRecordProducer { // TODO

  def produce(record: InsuranceRecord): Unit = {
    println(record)
  }
}
