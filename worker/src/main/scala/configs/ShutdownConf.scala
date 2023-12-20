package configs

import derevo.derive
import derevo.pureconfig.{pureconfigReader, pureconfigWriter}

import scala.concurrent.duration.FiniteDuration

@derive(pureconfigReader, pureconfigWriter)
final case class ShutdownConf(
    gracePeriod: FiniteDuration
)
