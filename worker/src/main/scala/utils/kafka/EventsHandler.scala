package utils.kafka

import cats.data.NonEmptyVector
import cats.{Applicative, FlatMap, Monad}
import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.traverse._
import cats.effect.Clock
import derevo.derive
import derevo.tagless.applyK
import fs2.kafka.ConsumerRecord
import tofu.WithProvide
import tofu.generate.GenUUID
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._

@derive(applyK)
trait EventsHandler[Err, F[_]] {
  def handleBatch(records: NonEmptyVector[ConsumerRecord[Option[String], Option[String]]]): F[HandleResult[Err]]
}

object EventsHandler {
  implicit final class EventsHandlerOps[Err, F[_]](private val eventsHandler: EventsHandler[Err, F]) extends AnyVal {
    def filterHeaders[HS: KafkaHeadersReader](headers: HS)(implicit
        applicative: Applicative[F]
    ): EventsHandler[Err, F] = new HeadersFilter(headers, eventsHandler)

    def withCtx[I[_], Ctx: KafkaHeadersReader](init: I[Ctx])(implicit
        genUUID: GenUUID[I],
        monad: Monad[I],
        withProvide: WithProvide[F, I, Ctx]
    ): EventsHandler[Err, I] =
      new WithKafkaContext[I, F, Err, Ctx](init, eventsHandler)
  }

  final private class Observer[Err, F[_]: FlatMap: Clock](observer: EventsObserver[Err, F])
    extends EventsHandler[Err, Mid[F, *]] {
    override def handleBatch(
        records: NonEmptyVector[ConsumerRecord[Option[String], Option[String]]]
    ): Mid[F, HandleResult[Err]] =
      fa =>
        Clock[F].timed(fa).flatMap {
          case (elapsed, res) =>
            observer.observe(records, res, elapsed).as(res)
        }
  }

  final private class Logs[Err, F[_]: FlatMap: Logging.Make] extends EventsHandler[Err, Mid[F, *]] {
    implicit val logging: Logging[F] = Logging.Make[F].forService[EventsHandler[Err, F]]

    override def handleBatch(
        records: NonEmptyVector[ConsumerRecord[Option[String], Option[String]]]
    ): Mid[F, HandleResult[Err]] =
      info"Started handling of records from ${records.head.offset} to ${records.last.offset} offset" >> _
  }

  final private class HeadersFilter[+HS: KafkaHeadersReader, Err, F[_]: Applicative](
      expected: HS,
      delegate: EventsHandler[Err, F]
  ) extends EventsHandler[Err, F] {
    override def handleBatch(
        records: NonEmptyVector[ConsumerRecord[Option[String], Option[String]]]
    ): F[HandleResult[Err]] = {
      val filteredRecords = records.filter { r =>
        KafkaHeadersReader[HS]
          .read(r.headers)
          .fold(false)(_ == expected)
      }

      NonEmptyVector
        .fromVector(filteredRecords)
        .fold(HandleResult.skip[Err].pure[F])(delegate.handleBatch)
    }
  }

  final private class WithKafkaContext[I[_]: GenUUID: Monad, F[_], Err, Ctx: KafkaHeadersReader](
      defaultCtx: I[Ctx],
      delegate: EventsHandler[Err, F]
  )(implicit
      withProvide: WithProvide[F, I, Ctx]
  ) extends EventsHandler[Err, I] {
    override def handleBatch(
        records: NonEmptyVector[ConsumerRecord[Option[String], Option[String]]]
    ): I[HandleResult[Err]] = {

      val headersCtx = KafkaHeadersReader[Ctx].read(records.head.headers)

      headersCtx.fold(defaultCtx)(_.pure[I]) >>= withProvide.runContext(delegate.handleBatch(records))
    }
  }

  def observer[Err, F[_]: FlatMap: Clock](observer: EventsObserver[Err, F]): EventsHandler[Err, Mid[F, *]] =
    new Observer[Err, F](observer)

  def logs[Err, F[_]: FlatMap: Logging.Make]: EventsHandler[Err, Mid[F, *]] = new Logs[Err, F]
}
