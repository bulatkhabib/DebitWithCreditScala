package utils.db

import cats.effect.{Async, MonadCancelThrow, Resource}
import configs.DatabaseConf
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import tofu.lift.Lift

object DatabaseTransactor {
  def make[I[_]: Async, F[_]: MonadCancelThrow](
      config: DatabaseConf
  )(implicit lift: Lift[I, F]): Resource[I, Transactor[F]] =
    for {
      connectEC <- ExecutionContexts.fixedThreadPool[I](
        config.awaitingThreads
      )
      doobieConfig = config.makeDoobieConfig
      initialTransactor <-
        HikariTransactor.fromConfig[I](doobieConfig, connectEC)
    } yield initialTransactor.mapK[F](lift.liftF)
}
