package models

import consumer.Domain.Consumer.LoanOrderReader.LoanOrderReaderEvent
import tethys._
import tethys.jackson._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class LoanOrderReaderEventTest extends AnyFlatSpec with Matchers {

  it should "test decoding loan order reader event" in {
    val jsonString =
      s"""
        {
           | "id" : "89a6171a-0521-4cdc-bbe7-08706973f944",
           | "userId": "89a6171a-0521-4cdc-bbe7-08706973f944",
           | "term": 20,
           | "children": "2",
           | "amount": "100000",
           | "averageMoney": 100000,
           | "workPeriod": 5,
           | "lastWorkPeriod": "2",
           | "birthDate": "21.01.1999"
        }
      """.stripMargin

    jsonString.jsonAs[LoanOrderReaderEvent.V1] shouldBe Right(
      LoanOrderReaderEvent.V1(
        id = UUID.fromString("89a6171a-0521-4cdc-bbe7-08706973f944"),
        userId = UUID.fromString("89a6171a-0521-4cdc-bbe7-08706973f944"),
        term = 20,
        children = "2",
        amount = "100000",
        averageMoney = 100000,
        workPeriod = 5,
        lastWorkPeriod = "2",
        birthDate = "21.01.1999"
      )
    )
  }
}
