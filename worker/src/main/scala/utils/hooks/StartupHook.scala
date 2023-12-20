package utils.hooks

import cats.FlatMap
import cats.effect.{ExitCode, MonadCancelThrow, Outcome}
import cats.syntax.flatMap._
import cats.syntax.functor._
import derevo.derive
import derevo.tagless.applyK
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._
import utils.probes.ProbeControl

@derive(applyK)
trait StartupHook[F[_]] {
  def start: F[ExitCode]
}

object StartupHook extends Logging.Companion[StartupHook] {

  final private class Impl[F[_]: MonadCancelThrow](
      loop: F[Unit],
      control: ProbeControl[F],
      shutdown: ShutdownHook[F]
  ) extends StartupHook[F] {
    override def start: F[ExitCode] =
      for {
        _ <- control.ready
        _ <- MonadCancelThrow[F].guaranteeCase(loop) {
          case Outcome.Canceled() => shutdown.shutdown
          case _                  => MonadCancelThrow[F].unit
        }
      } yield ExitCode.Success
  }

  final private class LogMid[F[_]: FlatMap: StartupHook.Log] extends StartupHook[Mid[F, *]] {
    override def start: Mid[F, ExitCode] =
      loop =>
        for {
          _   <- info"Application is ready"
          res <- loop
          _   <- warn"Application exited the main loop"
        } yield res
  }

  def log[F[_]: FlatMap: StartupHook.Log]: StartupHook[Mid[F, *]] = new LogMid[F]

  def impl[F[_]: MonadCancelThrow](
      loop: F[Unit],
      control: ProbeControl[F],
      shutdown: ShutdownHook[F]
  ): StartupHook[F] =
    new Impl[F](loop, control, shutdown)

  def default[F[_]: MonadCancelThrow: StartupHook.Log](
      loop: F[Unit],
      control: ProbeControl[F],
      shutdown: ShutdownHook[F]
  ): StartupHook[F] =
    log[F].attach(impl[F](loop, control, shutdown))
}
