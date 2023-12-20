package utils.kafka

import cats.Monad
import cats.data.NonEmptyVector
import cats.syntax.flatMap._
import fs2.kafka.ConsumerRecord
import tofu.Raise

trait ConsumerHandler[F[_]] {
  def handleBatch(records: Vector[ConsumerRecord[Option[String], Option[String]]]): F[Unit]
}

object ConsumerHandler {
  final private class FromEvents[Err, F[_]: Monad](eventsHandler: EventsHandler[Err, F])(implicit raise: Raise[F, Err])
    extends ConsumerHandler[F] {
    override def handleBatch(records: Vector[ConsumerRecord[Option[String], Option[String]]]): F[Unit] =
      NonEmptyVector.fromVector(records).fold(Monad[F].unit) { nonEmptyBatch =>
        eventsHandler.handleBatch(nonEmptyBatch).flatMap {
          case HandleResult.Ok      => Monad[F].unit
          case HandleResult.Skip    => Monad[F].unit
          case HandleResult.Fail(e) => raise.raise(e)
        }
      }
  }

  def batchFrom[Err, F[_]: Monad](
      eventsHandler: EventsHandler[Err, F]
  )(implicit raise: Raise[F, Err]): ConsumerHandler[F] =
    new FromEvents[Err, F](eventsHandler)
}
