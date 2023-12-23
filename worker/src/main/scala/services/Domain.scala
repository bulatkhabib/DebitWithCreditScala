package services

import db.Domain.LoanStatus
import derevo.derive
import derevo.tethys.{tethysReader, tethysWriter}
import io.estatico.newtype.macros.newtype
import tethys.{JsonReader, JsonWriter}

import java.util.UUID

object Domain {
  final case class LoanData(
      id: LoanData.LoanId,
      userId: LoanData.LoanUserId,
      term: LoanData.Term,
      amount: LoanData.Amount,
      averageMoney: LoanData.AverageMoney,
      workPeriod: Int,
      lastWorkPeriod: String
  )

  object LoanData {
    @derive(tethysReader, tethysWriter)
    @newtype case class LoanId(id: UUID)

    @derive(tethysReader, tethysWriter)
    @newtype case class LoanUserId(id: UUID)

    @derive(tethysReader, tethysWriter)
    @newtype case class Term(term: Int)

    @derive(tethysReader, tethysWriter)
    @newtype case class Amount(amount: String)

    @derive(tethysReader, tethysWriter)
    @newtype case class AverageMoney(averageMoney: Int)
  }

  implicit lazy val uuidReader: JsonReader[UUID] = JsonReader.stringReader.map(UUID.fromString)
  implicit lazy val uuidWriter: JsonWriter[UUID] = JsonWriter.stringWriter.contramap(_.toString)
}
