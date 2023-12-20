import cats.{Monad, Now}
import consumer.Domain.Consumer
import io.estatico.newtype.macros.newtype
import services.{LoanOrderReaderParser, OrderProcessingService}
import utils.kafka.EventsHandler

package object consumer {
  @newtype case class LoanOrderReader[F[_]](eventsHandler: EventsHandler[Consumer.LoanOrderReader.Error, F])

  object LoanOrderReader {
    def make[F[_]: Monad: Consumer.LoanOrderReader.Error.Handle](
        parser: LoanOrderReaderParser[F],
        service: OrderProcessingService[F]
    ): LoanOrderReader[F] =
      LoanOrderReader[F](
        new LoanOrderReaderV1[F](parser, service)
      )
  }
}
