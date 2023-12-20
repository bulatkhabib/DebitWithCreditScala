package services

import db.Domain.LoanStatus
import derevo.derive
import derevo.tethys.{tethysReader, tethysWriter}
import io.estatico.newtype.macros.newtype

object Domain {
  final case class LoanData(
      loanId: LoanData.LoanId,
      status: LoanStatus,
      userId: LoanData.LoanUserId,
      term: LoanData.Term,
      amount: LoanData.Amount,
      averageMoney: LoanData.AverageMoney,
      workPeriod: Int,
      lastWorkPeriod: Int
  )

  object LoanData {
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
}
