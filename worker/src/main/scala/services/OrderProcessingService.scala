package services

import cats.Monad
import cats.syntax.flatMap._
import cats.effect.kernel.Sync
import db.Domain.{LoanEntry, LoanStatus}
import db.LoanStorage
import services.Domain.LoanData
import tofu.time.Sleep

import scala.concurrent.duration.DurationInt

trait OrderProcessingService[F[_]] {
  def processOrder(loan: LoanData): F[Boolean]
}

object OrderProcessingService {
  final private class Impl[F[_]: Monad: Sleep](loanStorage: LoanStorage[F]) extends OrderProcessingService[F] {
    override def processOrder(loan: LoanData): F[Boolean] =
      if (loan.workPeriod > 2 && loan.lastWorkPeriod >= 1)
        Sleep[F].sleep(100.milliseconds) >> loanStorage.updateStatus(
          LoanEntry.LoanId(loan.loanId.id),
          LoanStatus.Approved
        )
      else
        Sleep[F].sleep(100.milliseconds) >> loanStorage.updateStatus(
          LoanEntry.LoanId(loan.loanId.id),
          LoanStatus.Declined
        )
  }

  def make[F[_]: Monad: Sleep](loanStorage: LoanStorage[F]): OrderProcessingService[F] =
    new Impl[F](loanStorage)
}
