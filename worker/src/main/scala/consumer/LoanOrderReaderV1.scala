package consumer

import cats.syntax.traverse._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.Monad
import cats.data.NonEmptyVector
import consumer.Domain.Consumer
import fs2.kafka.ConsumerRecord
import services.Domain.LoanData
import services.{LoanOrderReaderParser, OrderProcessingService}
import tofu.Handle
import tofu.syntax.handle._
import utils.kafka.{EventsHandler, HandleResult}

class LoanOrderReaderV1[F[_]: Monad: Handle[*[_], Consumer.LoanOrderReader.Error]](
    parser: LoanOrderReaderParser[F],
    service: OrderProcessingService[F]
) extends EventsHandler[Consumer.LoanOrderReader.Error, F] {
  override def handleBatch(
      records: NonEmptyVector[ConsumerRecord[Option[String], Option[String]]]
  ): F[HandleResult[Consumer.LoanOrderReader.Error]] =
    (for {
      eitherOfEvents <- records.traverse { record =>
        parser.fromRecordV1(record)
      }
      (_, events) = eitherOfEvents.toVector.partitionMap(identity)

      loadEvents = events.map { event =>
        LoanData(
          id = LoanData.LoanId(event.id),
          userId = LoanData.LoanUserId(event.userId),
          term = LoanData.Term(event.term),
          amount = LoanData.Amount(event.amount),
          averageMoney = LoanData.AverageMoney(event.averageMoney),
          workPeriod = event.workPeriod,
          lastWorkPeriod = event.lastWorkPeriod
        )
      }

      _ <- loadEvents.traverse { event =>
        service.createOrder(event)
      }

      _ <- loadEvents.traverse { event =>
        service.processOrder(event)
      }

    } yield HandleResult.ok[Consumer.LoanOrderReader.Error]).handle[Consumer.LoanOrderReader.Error] { error =>
      error.level match {
        case Consumer.Error.Level.Business => HandleResult.skip
        case _                             => HandleResult.fail(error)
      }
    }
}
