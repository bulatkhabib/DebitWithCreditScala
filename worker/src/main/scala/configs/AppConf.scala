package configs

import cats.effect.Sync
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.{ConfigReader, ConfigSource}
import utils.kafka.{KafkaBatchConf, KafkaConsumerConf}

@derive(pureconfigReader)
final case class AppConf(
    shutdown: ShutdownConf,
    loanOrderReader: KafkaConsumerConf,
    database: DatabaseConf
)

object AppConf {
  def load[I[_]: Sync]: I[AppConf] = Sync[I].delay(ConfigSource.default.loadOrThrow[AppConf])

  implicit val kafkaConsumerConfReader: ConfigReader[KafkaConsumerConf] = pureconfig.generic.semiauto.deriveReader
  implicit val kafkaBatchConfReader: ConfigReader[KafkaBatchConf]       = pureconfig.generic.semiauto.deriveReader
  implicit val databaseConfReader: ConfigReader[DatabaseConf]           = pureconfig.generic.semiauto.deriveReader
  implicit val hikariPoolConfReader: ConfigReader[HikariPoolConf]       = pureconfig.generic.semiauto.deriveReader
}
