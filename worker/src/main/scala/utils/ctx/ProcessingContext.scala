package utils.ctx

import cats.Functor
import cats.Functor
import cats.syntax.functor._
import derevo.derive
import derevo.tethys.{tethysReader, tethysWriter}
import fs2.kafka.{Header, HeaderDeserializer, Headers}
import io.estatico.newtype.macros.newtype
import sttp.tapir.SchemaType.SString
import sttp.tapir.{Codec, CodecFormat, Schema}
import tofu.generate.GenUUID
import tofu.logging.derivation.loggable
import utils.ctx.ProcessingContext.XTraceId
import utils.kafka.KafkaHeadersCodec

import java.util.UUID

@derive(loggable)
final case class ProcessingContext(
    traceId: XTraceId,
    serverRequest: Option[HttpServerRequest]
)

@derive(loggable)
final case class HttpServerRequest(
    operationName: String,
    method: String,
    uri: String
)

object ProcessingContext {
  def create[F[_]: GenUUID: Functor](serverRequest: Option[HttpServerRequest]): F[ProcessingContext] =
    TraceId.create[F].map(t => ProcessingContext(t.toXTraceId, serverRequest))

  implicit val headerAttemptString: HeaderDeserializer.Attempt[String] = HeaderDeserializer[String].attempt

  implicit val processingContext: KafkaHeadersCodec[ProcessingContext] =
    new KafkaHeadersCodec[ProcessingContext] {
      private val headerName: String = "x-trace-id"

      override def read(headers: Headers): Option[ProcessingContext] =
        headers(headerName)
          .flatMap(_.attemptAs[String].toOption.map(XTraceId.apply))
          .map(ProcessingContext(_, None))

      override def write(value: ProcessingContext): Headers =
        Headers(Header(headerName, value.traceId.id))
    }

  @derive(loggable)
  @newtype case class XTraceId(id: String)

  object XTraceId {
    def create[F[_]: GenUUID: Functor]: F[XTraceId] =
      GenUUID.randomString[F].map(apply)

    implicit val tapirCodec: Codec[String, XTraceId, CodecFormat.TextPlain] =
      Codec.string.map[XTraceId](apply _)(_.id)
  }

  @derive(tethysReader, tethysWriter, loggable)
  @newtype case class TraceId(id: UUID) {
    def toXTraceId: XTraceId = XTraceId(id.toString)
  }

  object TraceId {
    def create[F[_]: GenUUID: Functor]: F[TraceId] = GenUUID.random[F].map(apply)

    implicit val tapirCodec: Codec[String, TraceId, CodecFormat.TextPlain] =
      Codec.uuid.map[TraceId](apply _)(_.id)

    implicit val tapirSchema: Schema[TraceId] =
      Schema(SString[TraceId]()).description("Идентификатор запроса")
  }
}
