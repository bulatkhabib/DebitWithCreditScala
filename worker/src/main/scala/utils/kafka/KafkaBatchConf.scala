package utils.kafka

import scala.concurrent.duration.FiniteDuration

final case class KafkaBatchConf(
    maxSize: Int,
    maxTimeWindow: FiniteDuration
)
