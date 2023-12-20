import cats.data.ReaderT
import cats.effect.{ExitCode, IO, IOApp}
import utils.ctx.ProcessingContext

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    Application.run[IO, ReaderT[IO, ProcessingContext, *]]
      .use(_.start)
  }
}
