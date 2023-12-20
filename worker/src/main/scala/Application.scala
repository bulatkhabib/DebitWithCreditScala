import cats.Parallel
import cats.effect.{Async, Clock, Resource}
import cats.syntax.flatMap._
import sttp.tapir.server.http4s.Http4sServerOptions
import tofu.{Execute, WithRun}
import utils.ctx.ProcessingContext
import utils.hooks.StartupHook
import wiring.{CoreComponent, RunComponent}

object Application {

  def run[I[_]: Async: Clock, F[_]: Parallel: Async: Execute](implicit
      withRun: WithRun[F, I, ProcessingContext]
  ): Resource[I, StartupHook[I]] = {
    for {
      core: CoreComponent[I, F] <- Resource.eval(CoreComponent.make[I, F])
      services: ServiceComponent[F] <-
        Resource.eval(ServiceComponent.make[I, F](core, database))
      run: RunComponent[I] <-
        Resource.eval(RunComponent.make[I, F](core, services))
      hook: StartupHook[I] <- run.resource
    } yield hook
  }
}
