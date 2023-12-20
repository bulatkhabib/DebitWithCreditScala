package utils.kafka

import cats.syntax.flatMap._
import cats.{FlatMap, InvariantMonoidal, Monad, ~>}
import tofu.higherKind.{Mid, RepK, RepresentableK}
import tofu.logging.Logging
import tofu.syntax.logging._

trait ShutdownHandler[F[_]] {
  def onError(e: Throwable): F[Unit]

  def onSuccess: F[Unit]

  def onCancel: F[Unit]
}

object ShutdownHandler extends Logging.Companion[ShutdownHandler] {
  implicit def representableK[UF[_[_[_]]]]: UF[ShutdownHandler[*[_]]] =
    new RepresentableK[ShutdownHandler[*[_]]] {
      override def tabulate[F[_]](hom: RepK[ShutdownHandler[*[_]], *] ~> F): ShutdownHandler[F] =
        new ShutdownHandler[F] {
          override def onError(e: Throwable): F[Unit] =
            hom(RepK[ShutdownHandler[*[_]]](_.onError(e)))

          override def onSuccess: F[Unit] =
            hom(RepK[ShutdownHandler[*[_]]](_.onSuccess))

          override def onCancel: F[Unit] =
            hom(RepK[ShutdownHandler[*[_]]](_.onCancel))
        }
    }.asInstanceOf[UF[ShutdownHandler[*[_]]]]

  final private class Noop[F[_]: InvariantMonoidal] extends ShutdownHandler[F] {
    override def onError(e: Throwable): F[Unit] = InvariantMonoidal[F].unit

    override def onSuccess: F[Unit] = InvariantMonoidal[F].unit

    override def onCancel: F[Unit] = InvariantMonoidal[F].unit
  }

  final private class LogMid[F[_]: cats.FlatMap: ShutdownHandler.Log] extends ShutdownHandler[Mid[F, *]] {
    override def onError(e: Throwable): Mid[F, Unit] =
      _ >> errorCause"Consumer failed with" (e)

    override def onCancel: Mid[F, Unit] =
      _ >> info"Consumer was cancelled"

    override def onSuccess: Mid[F, Unit] =
      _ >> info"Consumer finished successfully"
  }

  def noop[F[_]: InvariantMonoidal]: ShutdownHandler[F] = new Noop[F]

  def logMid[F[_]: FlatMap: ShutdownHandler.Log]: ShutdownHandler[Mid[F, *]] = new LogMid[F]

  def default[F[_]: Monad: ShutdownHandler.Log]: ShutdownHandler[F] =
    logMid[F] attach noop[F]
}
