package configs

import doobie.hikari.Config

final case class DatabaseConf(
    driverClassName: Option[String],
    awaitingThreads: Int,
    hikariPool: HikariPoolConf
) {
  def makeDoobieConfig: Config =
    Config(
      jdbcUrl = Some(hikariPool.jdbcUrl),
      driverClassName = driverClassName,
      password = Some(hikariPool.password),
      username = Some(hikariPool.username),
      maximumPoolSize = Some(hikariPool.maximumPoolSize),
      maxLifetime = hikariPool.maxLifetime,
      minimumIdle = Some(hikariPool.minimumIdle),
      idleTimeout = hikariPool.idleTimeout,
      validationTimeout = hikariPool.validationTimeout,
      connectionTimeout = hikariPool.connectionTimeout,
      initializationFailTimeout = hikariPool.initializationFailTimeout,
      leakDetectionThreshold = hikariPool.leakDetectionThreshold,
      poolName = hikariPool.poolName
    )
}
