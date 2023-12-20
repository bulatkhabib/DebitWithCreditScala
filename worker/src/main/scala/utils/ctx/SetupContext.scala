package utils.ctx

import cats.syntax.flatMap._
import cats.{FlatMap, ~>}
import tofu.WithProvide
import tofu.syntax.funk._

trait SetupContext[I[_], F[_]] {
  def setup[A](fa: F[A]): I[A]

  def setupK: F ~> I = funK[F, I](setup)
}

object SetupContext {
  def make[I[_]: FlatMap, F[_], Ctx](from: I[Ctx])(implicit withProvide: WithProvide[F, I, Ctx]): SetupContext[I, F] =
    new SetupContext[I, F] {
      override def setup[A](fa: F[A]): I[A] = from >>= withProvide.runContext(fa)
    }

  def apply[I[_], F[_]](implicit setupContext: SetupContext[I, F]): SetupContext[I, F] = setupContext
}