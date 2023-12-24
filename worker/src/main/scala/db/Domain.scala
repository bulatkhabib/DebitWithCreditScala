package db

import derevo.derive
import derevo.tethys.{tethysReader, tethysWriter}
import enumeratum.values.{StringDoobieEnum, StringEnum, StringEnumEntry}
import io.estatico.newtype.macros.newtype
import tethys.{JsonReader, JsonWriter}
import tethys.enumeratum.StringTethysEnum

import java.util.UUID

object Domain {
  final case class LoanEntry(
       loanId: LoanEntry.LoanId,
       status: LoanStatus,
       userId: LoanEntry.LoanUserId,
       term: LoanEntry.Term,
       amount: LoanEntry.Amount,
       submissionDate: String,
       interestRate: String
  )

  object LoanEntry {
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

  sealed abstract class LoanStatus(
                                    val value: String
                                  ) extends StringEnumEntry

  object LoanStatus extends StringEnum[LoanStatus] with StringDoobieEnum[LoanStatus] with StringTethysEnum[LoanStatus] {
    case object Pending extends LoanStatus("PENDING")

    case object Approved extends LoanStatus("APPROVED")

    case object Declined extends LoanStatus("DECLINED")

    override val values: IndexedSeq[LoanStatus] = findValues
  }

  implicit lazy val uuidReader: JsonReader[UUID] = JsonReader.stringReader.map(UUID.fromString)
  implicit lazy val uuidWriter: JsonWriter[UUID] = JsonWriter.stringWriter.contramap(_.toString)
}
