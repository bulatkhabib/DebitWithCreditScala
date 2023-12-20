package db

import derevo.derive
import derevo.tethys.{tethysReader, tethysWriter}
import enumeratum.values.{StringDoobieEnum, StringEnum, StringEnumEntry}
import io.estatico.newtype.macros.newtype
import tethys.enumeratum.StringTethysEnum

object Domain {
  final case class LoanEntry(
      loanId: LoanEntry.LoanId,
      status: LoanStatus,
      userId: LoanEntry.LoanUserId,
      term: LoanEntry.Term,
      amount: LoanEntry.Amount,
      averageMoney: LoanEntry.AverageMoney
  )

  object LoanEntry {
    @derive(tethysReader, tethysWriter)
    @newtype case class LoanId(id: Int)

    @derive(tethysReader, tethysWriter)
    @newtype case class LoanUserId(id: Int)

    @derive(tethysReader, tethysWriter)
    @newtype case class Term(term: Int)

    @derive(tethysReader, tethysWriter)
    @newtype case class Amount(amount: BigDecimal)

    @derive(tethysReader, tethysWriter)
    @newtype case class AverageMoney(averageMoney: BigDecimal)
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
}
