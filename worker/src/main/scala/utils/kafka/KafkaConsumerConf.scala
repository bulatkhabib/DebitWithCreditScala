package utils.kafka

import scala.concurrent.duration.FiniteDuration

final case class KafkaConsumerConf(
    topic: String,
    parallelism: Int,
    shutdownTimeout: FiniteDuration,
    bootstrapServers: Vector[String],
    batch: KafkaBatchConf,
    properties: Map[String, String]
)
