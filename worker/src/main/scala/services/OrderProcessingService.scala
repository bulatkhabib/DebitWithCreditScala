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
  def createOrder(loan: LoanData): F[Boolean]
}

object OrderProcessingService {
  final private class Impl[F[_]: Monad: Sleep](loanStorage: LoanStorage[F]) extends OrderProcessingService[F] {
    override def processOrder(loan: LoanData): F[Boolean] =
      if (loan.workPeriod > 2 && loan.lastWorkPeriod.toInt >= 1)
        Sleep[F].sleep(100.milliseconds) >> loanStorage.updateStatus(
          LoanEntry(
            loanId = LoanEntry.LoanId(loan.id.id),
            status = LoanStatus.Approved,
            userId = LoanEntry.LoanUserId(loan.userId.id),
            term = LoanEntry.Term(loan.term.term),
            amount = LoanEntry.Amount(loan.amount.amount),
            submissionDate = "23.12.2023",
            interestRate = loan.interestRate
          ),
          LoanStatus.Approved
        )
      else
        Sleep[F].sleep(100.milliseconds) >> loanStorage.updateStatus(
          LoanEntry(
            loanId = LoanEntry.LoanId(loan.id.id),
            status = LoanStatus.Declined,
            userId = LoanEntry.LoanUserId(loan.userId.id),
            term = LoanEntry.Term(loan.term.term),
            amount = LoanEntry.Amount(loan.amount.amount),
            submissionDate = "23.12.2023",
            interestRate = loan.interestRate
          ),
          LoanStatus.Declined
        )

    override def createOrder(loan: LoanData): F[Boolean] =
      loanStorage.create(
        LoanEntry(
          loanId = LoanEntry.LoanId(loan.id.id),
          status = LoanStatus.Pending,
          userId = LoanEntry.LoanUserId(loan.userId.id),
          term = LoanEntry.Term(loan.term.term),
          amount = LoanEntry.Amount(loan.amount.amount),
          submissionDate = "23.12.2023",
          interestRate = loan.interestRate
        )
      )
  }

  def make[F[_]: Monad: Sleep](loanStorage: LoanStorage[F]): OrderProcessingService[F] =
    new Impl[F](loanStorage)
}
