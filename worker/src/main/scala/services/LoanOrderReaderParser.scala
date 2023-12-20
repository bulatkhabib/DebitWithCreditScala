package services

import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.Monad
import consumer.Domain.Consumer.LoanOrderReader.Error.LoanOrderReaderParserError
import consumer.Domain.Consumer.LoanOrderReader.LoanOrderReaderEvent
import derevo.derive
import derevo.tagless.applyK
import fs2.kafka.ConsumerRecord
import tethys._
import tethys.jackson._
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.handle._
import tofu.syntax.logging._
import tofu.syntax.raise._

@derive(applyK)
trait LoanOrderReaderParser[F[_]] {
  def fromRecordV1(
      record: ConsumerRecord[Option[String], Option[String]]
  ): F[Either[LoanOrderReaderParserError, LoanOrderReaderEvent.V1]]
}

object LoanOrderReaderParser extends Logging.Companion[LoanOrderReaderParser] {
  final private class Impl[F[_]: Monad: LoanOrderReaderParserError.Errors] extends LoanOrderReaderParser[F] {
    override def fromRecordV1(
        record: ConsumerRecord[Option[String], Option[String]]
    ): F[Either[LoanOrderReaderParserError, LoanOrderReaderEvent.V1]] =
      (for {
        recordValue <- record.value.orRaise[F](LoanOrderReaderParserError.EventEmpty)
        event <- recordValue.jsonAs[LoanOrderReaderEvent.V1]
          .leftMap(LoanOrderReaderParserError.EventInvalid).toRaise[F]
      } yield event).attempt[LoanOrderReaderParserError]
  }

  final private class Logs[F[_]: Monad: LoanOrderReaderParserError.Errors: LoanOrderReaderParser.Log]
    extends LoanOrderReaderParser[Mid[F, *]] {
    override def fromRecordV1(
        record: ConsumerRecord[Option[String], Option[String]]
    ): Mid[F, Either[LoanOrderReaderParserError, LoanOrderReaderEvent.V1]] =
      _.flatTap {
        case Left(error) =>
          warn"Parsing of command v1 failed for record with offset=${record.offset}, topic=${record.topic}. $error" *>
            debug"Parsing of command v1 failed from record ${record.value}. $error"
        case _ =>
          debug"Parsing of command v1 successed for record ${record.value} with offset=${record.offset}, topic=${record.topic}"
      }
  }

  def makeObservable[F[_]: Monad: LoanOrderReaderParserError.Errors: LoanOrderReaderParser.Log]
      : LoanOrderReaderParser[F] =
    new Logs[F] attach new Impl[F]
}
