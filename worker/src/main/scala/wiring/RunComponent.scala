package wiring

import cats.syntax.functor._
import cats.syntax.monadError._
import cats.syntax.semigroup._
import cats.syntax.traverse._
import cats.tagless.syntax.functorK._
import cats.effect.{Async, Concurrent, Deferred, Resource}
import consumer.Domain.Consumer.LoanOrderReader
import tofu.WithProvide
import tofu.lift.Lift
import tofu.syntax.lift._
import tofu.syntax.monoid._
import utils.ctx.ProcessingContext
import utils.hooks.{ShutdownHook, StartupHook}
import utils.kafka.{Consumer, EventsObserver, ShutdownHandler}

class RunComponent[I[_]: Concurrent](
    val workers: List[Resource[I, Unit]],
    val hook: StartupHook[I]
) {
  def resource: Resource[I, StartupHook[I]] =
    (for {
      rWorkers <- workers.sequence
    } yield rWorkers).map(_ => hook)
}

object RunComponent {
  def make[I[_]: Async, F[_]: Async](
      core: CoreComponent[I, F],
      services: ServiceComponent[F]
  )(implicit
      lift: Lift[I, F],
      withProvide: WithProvide[F, I, ProcessingContext]
  ): I[RunComponent[I]] = {
    import core._

    val shutdownHook = ShutdownHook.default[F](conf.shutdown.gracePeriod, probeControl)

    val eventsObserverLoanOrderReader =
      EventsObserver.logs[LoanOrderReader.Error, F]

    val eventsHandlerLoanOrderReader =
      (EventsHandler.logs[LoanOrderReader.Error, F] |+| EventsHandler.observer[LoanOrderReader.Error, F](
        eventsObserverLoanOrderReader
      ))
        .attach(services.loanOrderReader.eventsHandler)

    Deferred[I, Either[Throwable, Unit]].map { stopSignal =>
      val startupHook: StartupHook[I] =
        StartupHook.default[F](
          stopSignal.get.rethrow.lift,
          probeControl,
          shutdownHook
        ).mapK()

      new RunComponent[I](
        List(
          Consumer.make[I](
            conf.loanOrderReader,
            ConsumerHandler.batchFrom(eventsHandlerLoanOrderReader),
            ShutdownHandler.default[I]
          )
        ),
        startupHook
      )
    }
  }
}
