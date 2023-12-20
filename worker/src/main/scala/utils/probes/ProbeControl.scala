package utils.probes

import cats.Functor
import cats.effect.Ref
import cats.syntax.functor._
import tofu.concurrent.MakeRef

final class ProbeControl[F[_]] private (ref: Ref[F, ProbeStatus]) {
  def status: F[ProbeStatus] = ref.get
  def ready: F[Unit]         = ref.set(ProbeStatus.Ready)
  def notReady: F[Unit]      = ref.set(ProbeStatus.NotReady)
}

object ProbeControl {
  def make[I[_]: Functor, F[_]](implicit makeRef: MakeRef[I, F]): I[ProbeControl[F]] =
    makeRef.refOf[ProbeStatus](ProbeStatus.NotReady).map(new ProbeControl[F](_))
}
