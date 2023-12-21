package wiring

import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.Monad
import cats.effect.{Async, Concurrent}
import consumer.LoanOrderReader
import services.{LoanOrderReaderParser, OrderProcessingService}
import tofu.lift.Lift
import tofu.time.Sleep

class ServiceComponent[F[_]](implicit
    val orderProcessingService: OrderProcessingService[F],
    val loanOrderReader: LoanOrderReader[F]
)

object ServiceComponent {
  def make[I[_]: Sleep: Async, F[_]: Async](
      core: CoreComponent[I, F],
      databaseComponent: DatabaseComponent[F]
  ): I[ServiceComponent[F]] = {
    import core._

    def makeOrderProccessingService(): I[OrderProcessingService[F]] =
      Async[I].delay(OrderProcessingService.make[F](databaseComponent.loanStorage))

    def makeLoanOrderReader(orderProcessingService: OrderProcessingService[F]): I[LoanOrderReader[F]] =
      Async[I].delay(LoanOrderReader.make[F](LoanOrderReaderParser.makeObservable, orderProcessingService))

    for {
      implicit0(orderProcessingService: OrderProcessingService[F]) <- makeOrderProccessingService()
      implicit0(loanOrderReader: LoanOrderReader[F])               <- makeLoanOrderReader(orderProcessingService)
    } yield new ServiceComponent[F]
  }
}
