package wiring

import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.Monad
import cats.effect.{Async, Concurrent}
import services.OrderProcessingService
import tofu.lift.Lift
import tofu.time.Sleep

class ServiceComponent[F[_]](implicit
    val orderProcessingService: OrderProcessingService[F]
)

object ServiceComponent {
  def make[I[_]: Monad: Sleep: Async, F[_]: Async](
      databaseComponent: DatabaseComponent[F]
  ): I[ServiceComponent[F]] = {

    def makeOrderProccessingService(): I[OrderProcessingService[F]] =
      Async[I].delay(OrderProcessingService.make[F](databaseComponent.loanStorage))

    for {
      implicit0(orderProcessingService: OrderProcessingService[F]) <- makeOrderProccessingService()
    } yield new ServiceComponent[F]
  }
}
