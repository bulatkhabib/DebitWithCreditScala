package utils.kafka

import cats.Apply
import cats.data.NonEmptyVector
import cats.kernel.Semigroup
import cats.syntax.functor._
import derevo.derive
import derevo.tagless.applyK
import fs2.kafka.ConsumerRecord
import tofu.logging.{Loggable, Logging}
import tofu.syntax.logging._

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

@derive(applyK)
trait EventsObserver[Err, F[_]] {
  def observe(
      records: NonEmptyVector[ConsumerRecord[Option[String], Option[String]]],
      result: HandleResult[Err],
      elapsed: FiniteDuration
  ): F[Unit]
}

object EventsObserver {
  final private class Sum[Err, F[_]: Apply](
      left: EventsObserver[Err, F],
      right: EventsObserver[Err, F]
  ) extends EventsObserver[Err, F] {
    override def observe(
        records: NonEmptyVector[ConsumerRecord[Option[String], Option[String]]],
        result: HandleResult[Err],
        elapsed: FiniteDuration
    ): F[Unit] =
      Apply[F].product(
        left.observe(records, result, elapsed),
        right.observe(records, result, elapsed)
      ).void
  }

  implicit def semigroup[Err, F[_]: Apply]: Semigroup[EventsObserver[Err, F]] =
    (l, r) => new Sum[Err, F](l, r)

  final private class Logs[Err: Loggable, F[_]: Logging.Make] extends EventsObserver[Err, F] {
    implicit val logging: Logging[F] = Logging.Make[F].forService[EventsObserver[Err, F]]

    override def observe(
        records: NonEmptyVector[ConsumerRecord[Option[String], Option[String]]],
        result: HandleResult[Err],
        elapsed: FiniteDuration
    ): F[Unit] = {
      val elapsedMs  = elapsed.toUnit(TimeUnit.MILLISECONDS)
      val headOffset = records.head.offset
      val lastOffset = records.last.offset

      result match {
        case HandleResult.Ok =>
          infoWith"Processing of events with offset from $headOffset to $lastOffset finished successfully in $elapsedMs ms" (
            (
              "consumer.processing.elapsed",
              elapsedMs
            )
          )
        case HandleResult.Skip =>
          warnWith"Skipping events with offset from $headOffset to $lastOffset in $elapsedMs ms" (
            (
              "consumer.processing.elapsed",
              elapsedMs
            )
          )
        case HandleResult.Fail(e) =>
          errorWith"Processing of events with offset from $headOffset to $lastOffset failed in $elapsedMs ms" (
            ("consumer.processing.error", e),
            ("consumer.processing.elapsed", elapsedMs)
          )
      }
    }
  }

  def logs[Err: Loggable, F[_]: Logging.Make]: EventsObserver[Err, F] =
    new Logs[Err, F]
}
