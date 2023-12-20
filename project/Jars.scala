import sbt.librarymanagement.syntax._

object Jars {
  object cats {
    val coreVersion = "2.8.0"
    val effectVersion = "3.5.2"

    val core = "org.typelevel" %% "cats-core" % coreVersion
    val effect = "org.typelevel" %% "cats-effect" % effectVersion
  }

  object tofu {
    val version = "0.12.0.1"

    val all = "tf.tofu" %% "tofu" % version
    val core = "tf.tofu" %% "tofu-core-ce3" % version
    val logging = "tf.tofu" %% "tofu-logging-layout" % version
    val derivation = "tf.tofu" %% "tofu-logging-derivation" % version
  }

  object derevo {
    val version = "0.13.0"

    val catsTagless = "tf.tofu" %% "derevo-cats-tagless" % version
    val derevoConfig = "tf.tofu" %% "derevo-pureconfig" % version
    val tethys = "tf.tofu" %% "derevo-tethys" % version
    val tethysMagnolia = "tf.tofu" %% "derevo-tethys-magnolia" % version
  }

  object tethys {
    val version = "0.26.0"

    val enumeratum = "com.tethys-json" %% "tethys-enumeratum" % version
  }

  object fs2 {
    val version = "3.2.4"

    val kafka = "com.github.fd4s" %% "fs2-kafka" % "2.2.0"
  }

  object http4s {
    object blaze {
      val version = "0.23.12"

      val server = "org.http4s" %% "http4s-blaze-server" % version
    }
  }

  object estatico {
    val version = "0.4.4"

    val newtype = "io.estatico" %% "newtype" % version
  }

  object tapir {
    val version = "1.1.3"

    val core = "com.softwaremill.sttp.tapir" %% "tapir-core" % version
    val sttpClient = "com.softwaremill.sttp.tapir" %% "tapir-sttp-client" % version
    val tethys = "com.softwaremill.sttp.tapir" %% "tapir-json-tethys" % version
    val http4s = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % version
    val enumeratum = "com.softwaremill.sttp.tapir" %% "tapir-enumeratum" % version
    val swagger = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % version
  }
}
