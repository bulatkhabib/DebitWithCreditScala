package consumer

import cats.syntax.semigroup._
import derevo.derive
import derevo.tethys.{tethysReader, tethysWriter}
import tofu.logging.{DictLoggable, LogRenderer, Loggable}
import tofu.{Errors => TofuErrors}

import scala.util.control.NoStackTrace

object Domain {
  object Consumer {
    object Error {
      sealed abstract class Level

      object Level {
        case object Business extends Level

        case object Internal extends Level
      }

      sealed abstract class Proto(
          val code: String,
          val message: String,
          val level: Level
      ) extends Throwable
          with NoStackTrace

      object Proto extends TofuErrors.Companion[Proto] {
        implicit val loggable: Loggable[Proto] =
          new DictLoggable[Proto] {
            override def fields[I, V, R, S](a: Proto, i: I)(implicit r: LogRenderer[I, V, R, S]): R =
              r.addString("error_code", a.code, i) |+|
                r.addString("error_message", a.message, i) |+|
                r.addString("error_level", a.level.toString, i)

            override def logShow(a: Proto): String = ""
          }
      }
    }

    object LoanOrderReader {
      sealed abstract class Error(
          override val code: String,
          override val message: String,
          override val level: Consumer.Error.Level
      ) extends Consumer.Error.Proto(code, message, level)

      object Error extends TofuErrors.Companion[Error] {
        implicit val loggable: Loggable[Error] =
          Loggable[Consumer.Error.Proto].narrow[Error]

        sealed abstract class LoanOrderReaderParserError(
            override val code: String,
            override val message: String,
            override val level: Consumer.Error.Level
        ) extends LoanOrderReader.Error(code, message, level)

        object LoanOrderReaderParserError extends TofuErrors.Companion[LoanOrderReaderParserError] {
          implicit val loggable: Loggable[LoanOrderReaderParserError] =
            Loggable[Error].narrow[LoanOrderReaderParserError]

          final case object EventEmpty
            extends LoanOrderReaderParserError(
              code = "BODY_IS_EMPTY",
              message = "",
              level = Consumer.Error.Level.Business
            )

          final case class EventInvalid(cause: tethys.readers.ReaderError)
            extends LoanOrderReaderParserError(
              code = "INVALID_EVENT",
              message = cause.getMessage,
              level = Consumer.Error.Level.Business
            )
        }
      }

      sealed trait LoanOrderReaderEvent

      object LoanOrderReaderEvent {
        @derive(tethysReader, tethysWriter)
        final case class V1(
            id: Int,
            userId: Int,
            term: Int,
            children: Int,
            amount: BigDecimal,
            averageMoney: BigDecimal,
            workPeriod: Int,
            lastWorkPeriod: Int,
            birthDate: String
        )
      }
    }
  }
}
