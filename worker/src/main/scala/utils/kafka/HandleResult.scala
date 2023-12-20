package utils.kafka

sealed trait HandleResult[+E]

object HandleResult {
  case object Ok                 extends HandleResult[Nothing]
  case object Skip               extends HandleResult[Nothing]
  final case class Fail[E](e: E) extends HandleResult[E]

  def ok[E]: HandleResult[E]         = Ok
  def skip[E]: HandleResult[E]       = Skip
  def fail[E](e: E): HandleResult[E] = Fail(e)
}
