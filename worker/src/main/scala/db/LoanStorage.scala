package db

import cats.effect.MonadCancelThrow
import cats.tagless.syntax.functorK._
import db.Domain.{LoanEntry, LoanStatus}
import derevo.derive
import derevo.tagless.applyK
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.ConnectionIO

@derive(applyK)
trait LoanStorage[F[_]] {
  def create(loan: LoanEntry): F[Boolean]

  def updateStatus(loan: LoanEntry, status: LoanStatus): F[Boolean]
}

object LoanStorage {
  private object DB extends LoanStorage[ConnectionIO] {
    override def create(loan: LoanEntry): ConnectionIO[Boolean] =
      sql"""
            INSERT INTO loan_application(
                id,
                amount,
                interest_rate,
                status,
                submission_date,
                term,
                user_id
            ) VALUES (
                ${loan.loanId.id},
                ${loan.amount.amount},
                ${loan.interestRate.toInt},
                ${loan.status},
                ${loan.submissionDate},
                ${loan.term.term},
                ${loan.userId.id}
            )
         """
        .update.run.map(_ > 0)

    override def updateStatus(loan: LoanEntry, status: LoanStatus): ConnectionIO[Boolean] =
      sql"""
        UPDATE
          loan_application
        SET
          status = $status
        WHERE
          id = ${loan.loanId.id}
          AND
          user_id = ${loan.userId.id}
      """.update.run.map(_ > 0)
  }

  def make[F[_]: MonadCancelThrow](
      transactor: Transactor[F]
  ): LoanStorage[F] =
    DB.mapK(transactor.trans)
}
