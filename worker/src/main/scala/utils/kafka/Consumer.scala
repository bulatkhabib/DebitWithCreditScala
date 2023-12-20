package utils.kafka

import cats.effect.kernel.Outcome
import cats.effect.syntax.monadCancel._
import cats.effect.{Async, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.kafka.{CommittableOffsetBatch, ConsumerSettings, KafkaConsumer}
import tofu.syntax.handle._
import tofu.time.Timeout
object Consumer {

  def make[F[_]: Async: Timeout](
      config: KafkaConsumerConf,
      handler: ConsumerHandler[F],
      shutdownHandler: ShutdownHandler[F],
  ): Resource[F, Unit] = {
    val settings =
      ConsumerSettings[F, Option[String], Option[String]]
        .withBootstrapServers(config.bootstrapServers.mkString(","))
        .withProperties(config.properties)

    Async[F].background[Unit](
      startConsumer(settings, config, handler, shutdownHandler)
        .attempt.foreverM
    ).map(_.void)
  }

  private def startConsumer[F[_]: Async](
      settings: ConsumerSettings[F, Option[String], Option[String]],
      config: KafkaConsumerConf,
      handler: ConsumerHandler[F],
      shutdownHandler: ShutdownHandler[F]
  )(implicit monitoring: ConsumerMonitoring[F]): F[Unit] =
    KafkaConsumer.resource(settings).use { consumer =>
      for {
        _ <- monitoring.start(consumer)
        _ <- consumer.subscribeTo(config.topic)
        _ <- consumer.records.groupWithin(config.batch.maxSize, config.batch.maxTimeWindow)
          .parEvalMap(config.parallelism) {
            chunk =>
              handler.handleBatch(chunk.map(_.record).toVector).as(chunk.last.map(_.offset))
          }
          .evalMap(CommittableOffsetBatch.fromFoldable(_).commit)
          .compile
          .drain
      } yield ()
    }.guaranteeCase {
      case Outcome.Errored(e)   => shutdownHandler.onError(e)
      case Outcome.Canceled()   => shutdownHandler.onCancel
      case Outcome.Succeeded(_) => shutdownHandler.onSuccess
    }
}
