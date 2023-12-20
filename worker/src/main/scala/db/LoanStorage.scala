package db

import cats.effect.MonadCancelThrow
import cats.tagless.syntax.functorK._
import derevo.derive
import derevo.tagless.applyK
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.ConnectionIO

@derive(applyK)
trait LoanStorage[F[_]] {
  def create(loan: LoanEntry): F[Boolean]

  def updateStatus(id: LoanId, status: LoanStatus): F[Boolean]
}

object LoanStorage {
  private object DB extends LoanStorage[ConnectionIO] {
    override def create(loan: LoanEntry): ConnectionIO[Boolean] =
      sql"""
            INSERT INTO loan_table(
                loan_id,
                user_id,
                term,
                amount,
                averageMoney
            ) VALUES (
                ${loan.loanId},
                ${loan.userId},
                ${loan.term},
                ${loan.amount},
                ${loan.averageMoney}
            )
         """
        .update.run.map(_ > 0)

    override def updateStatus(id: LoanId, status: LoanStatus): ConnectionIO[Boolean] =
      sql"""
        UPDATE
          loan_table
        SET
          status = ${status}
        WHERE
          loan_id = $id 
      """.update.run.map(_ > 0)
  }

  def make[F[_]: MonadCancelThrow](
      transactor: Transactor[F]
  ): LoanStorage[F] =
    DB.mapK(transactor.trans)
}
