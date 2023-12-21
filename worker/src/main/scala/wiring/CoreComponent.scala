package wiring

import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.effect.Async
import configs.AppConf
import tofu.{Delay, Fire, WithRun}
import tofu.logging.Logging
import tofu.time.Sleep
import utils.ctx.{ProcessingContext, SetupContext}
import utils.hooks.ShutdownHook
import utils.probes.ProbeControl

class CoreComponent[I[_], F[_]](implicit
    val conf: AppConf,
    val logMakerI: Logging.Make[I],
    val logMakerF: Logging.Make[F],
    val probeControl: ProbeControl[F],
    val shutdownHook: ShutdownHook[F],
    val setupContext: SetupContext[I, F],
)

object CoreComponent {
  def make[I[_]: Async: Fire: Delay: Sleep, F[_]: Async](implicit
      withRun: WithRun[F, I, ProcessingContext]
  ): I[CoreComponent[I, F]] = {
    for {
      implicit0(conf: AppConf) <- AppConf.load[I]
      implicit0(logMakeI: Logging.Make[I]) = Logging.Make.plain[I]
      implicit0(logMakeF: Logging.Make[F]) = Logging.Make.contextual[F, ProcessingContext]
      implicit0(probeControl: ProbeControl[F]) <- ProbeControl.make[I, F]
      implicit0(shutdownHook: ShutdownHook[F]) = ShutdownHook.default[F](conf.shutdown.gracePeriod, probeControl)
      implicit0(setupContext: SetupContext[I, F]) =
        SetupContext.make[I, F, ProcessingContext](ProcessingContext.create[I](None))
      comp = new CoreComponent[I, F]
    } yield comp
  }
}
