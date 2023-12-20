package configs

import scala.concurrent.duration.Duration

final case class HikariPoolConf(
    jdbcUrl: String,
    username: String,
    password: String,
    maximumPoolSize: Int,
    maxLifetime: Duration,
    minimumIdle: Int,
    idleTimeout: Duration,
    validationTimeout: Duration,
    connectionTimeout: Duration,
    initializationFailTimeout: Duration,
    leakDetectionThreshold: Duration,
    poolName: Option[String] = None
)
