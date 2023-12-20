package consumer

import cats.syntax.traverse._
import cats.Monad
import cats.data.NonEmptyVector
import consumer.Domain.Consumer
import fs2.kafka.ConsumerRecord
import tofu.Handle
import utils.kafka.HandleResult

class LoanOrderReader[F[_]: Monad: Handle[*[_], Consumer.LoanOrderReader.Error]](
    parser: LoanOrderReaderParser[F],
    service: LoanOrderReaderService[F]
) extends EventsHandler[Consumer.LoanOrderReader.Error, F] {
  override def handleBatch(
      records: NonEmptyVector[ConsumerRecord[Option[String], Option[String]]]
  ): F[HandleResult[Consumer.LoanOrderReader.Error]] =
    (for {
      eitherOfEvents <- records.traverse { record =>
        parser.fromRecord(record)
      }

    } yield HandleResult.ok[Consumer.LoanOrderReader.Error]).handle[Consumer.LoanOrderReader.Error] { error =>
      error.level match {
        case Consumer.Error.Level.Business => HandleResult.skip
        case _                             => HandleResult.fail(error)
      }
    }
}
