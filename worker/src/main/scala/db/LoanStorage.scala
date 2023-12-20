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

  def updateStatus(id: LoanEntry.LoanId, status: LoanStatus): F[Boolean]
}

object LoanStorage {
  private object DB extends LoanStorage[ConnectionIO] {
    override def create(loan: LoanEntry): ConnectionIO[Boolean] =
      sql"""
            INSERT INTO loan_table(
                loan_id,
                status,
                user_id,
                term,
                amount,
                averageMoney
            ) VALUES (
                ${loan.loanId.id},
                ${loan.status}
                ${loan.userId.id},
                ${loan.term.term},
                ${loan.amount.amount},
                ${loan.averageMoney.averageMoney}
            )
         """
        .update.run.map(_ > 0)

    override def updateStatus(id: LoanEntry.LoanId, status: LoanStatus): ConnectionIO[Boolean] =
      sql"""
        UPDATE
          loan_table
        SET
          status = $status
        WHERE
          loan_id = ${id.id}
      """.update.run.map(_ > 0)
  }

  def make[F[_]: MonadCancelThrow](
      transactor: Transactor[F]
  ): LoanStorage[F] =
    DB.mapK(transactor.trans)
}
