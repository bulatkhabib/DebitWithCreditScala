package utils.probes

sealed trait ProbeStatus
object ProbeStatus {
  case object Ready    extends ProbeStatus
  case object NotReady extends ProbeStatus
}
