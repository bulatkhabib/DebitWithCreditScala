package utils.hooks

import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{FlatMap, InvariantMonoidal, Monad}
import derevo.derive
import derevo.tagless.applyK
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._
import tofu.syntax.monoid._
import tofu.time.Sleep
import utils.probes.ProbeControl

import scala.concurrent.duration.FiniteDuration

@derive(applyK)
trait ShutdownHook[F[_]] {
  def shutdown: F[Unit]
}

object ShutdownHook extends Logging.Companion[ShutdownHook] {

  final private class TimedHook[F[_]: FlatMap: Sleep](delay: FiniteDuration) extends ShutdownHook[Mid[F, *]] {
    override def shutdown: Mid[F, Unit] = Sleep[F].sleep(delay) >> _
  }

  final private class LoggingHook[F[_]: ShutdownHook.Log: FlatMap] extends ShutdownHook[Mid[F, *]] {
    override def shutdown: Mid[F, Unit] =
      hook =>
        for {
          _ <- info"Application is shutting down"
          _ <- hook
        } yield ()
  }

  final private class ProbeHook[F[_]: FlatMap](probeControl: ProbeControl[F]) extends ShutdownHook[Mid[F, *]] {
    override def shutdown: Mid[F, Unit] = probeControl.notReady >> _
  }

  final private class Noop[F[_]: InvariantMonoidal] extends ShutdownHook[F] {
    override def shutdown: F[Unit] = InvariantMonoidal[F].unit
  }

  def noop[F[_]: InvariantMonoidal]: ShutdownHook[F]                               = new Noop[F]
  def log[F[_]: ShutdownHook.Log: FlatMap]: ShutdownHook[Mid[F, *]]                = new LoggingHook[F]
  def timed[F[_]: FlatMap: Sleep](delay: FiniteDuration): ShutdownHook[Mid[F, *]]  = new TimedHook[F](delay)
  def probe[F[_]: FlatMap](probeControl: ProbeControl[F]): ShutdownHook[Mid[F, *]] = new ProbeHook[F](probeControl)

  def default[F[_]: Monad: Sleep: ShutdownHook.Log](
      delay: FiniteDuration,
      control: ProbeControl[F]
  ): ShutdownHook[F] = {
    val mid: ShutdownHook[Mid[F, *]] = log[F] |+| probe[F](control) |+| timed[F](delay)

    mid attach noop[F]
  }
}
