package wiring

import cats.effect.Resource
import cats.effect.kernel.Async
import db.LoanStorage
import doobie.Transactor
import tofu.lift.Lift
import utils.db.DatabaseTransactor

class DatabaseComponent[F[_]](implicit
    val transactor: Transactor[F],
    val loanStorage: LoanStorage[F]
)

object DatabaseComponent {
  def make[I[_]: Async, F[_]: Async](core: CoreComponent[I, F])(implicit
      lift: Lift[I, F]
  ): Resource[I, DatabaseComponent[F]] = {
    import core._
    for {
      implicit0(transactor: Transactor[F]) <- DatabaseTransactor.make[I, F](conf.database)
      implicit0(loanStorage: LoanStorage[F]) = LoanStorage.make[F](transactor)
      comp                                   = new DatabaseComponent[F]
    } yield comp
  }
}
