import sbt._
import sbt.librarymanagement.ModuleID

object Dependencies {
  import Jars._

  object Worker {
    protected def distributionDependencies: Vector[ModuleID] =
      Vector(
        cats.core,
        cats.effect,
        tofu.core,
        tofu.logging,
        tofu.derivation,
        derevo.catsTagless,
        derevo.derevoConfig,
        derevo.tethys,
        derevo.tethysMagnolia,
        tethys.enumeratum,
        fs2.kafka,
        http4s.blaze.server,
        estatico.newtype,
        tapir.core,
        tapir.tethys,
        tapir.http4s,
        tapir.sttpClient
      )

    protected def testDependencies: Vector[ModuleID] = Vector.empty

    def overridingDependencies: Vector[ModuleID] = Vector.empty

    def dependencies: Vector[ModuleID] = distributionDependencies ++ testDependencies.map(_ % Test)
  }
}