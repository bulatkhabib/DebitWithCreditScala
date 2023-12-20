package utils.kafka

import fs2.kafka.{HeaderDeserializer, Headers}

trait KafkaHeadersReader[+A] {
  def read(headers: Headers): Option[A]
}

object KafkaHeadersReader extends KafkaHeadersInstances {
  def apply[A](implicit reader: KafkaHeadersReader[A]): KafkaHeadersReader[A] = reader
}

trait KafkaHeadersWriter[-A] {
  def write(value: A): Headers
}

object KafkaHeadersWriter extends KafkaHeadersInstances {
  def apply[A](implicit writer: KafkaHeadersWriter[A]): KafkaHeadersWriter[A] = writer
}

trait KafkaHeadersCodec[A] extends KafkaHeadersReader[A] with KafkaHeadersWriter[A]
object KafkaHeadersCodec   extends KafkaHeadersInstances

trait KafkaHeadersInstances {
  implicit val headerAttemptString: HeaderDeserializer.Attempt[String] = HeaderDeserializer[String].attempt
}
