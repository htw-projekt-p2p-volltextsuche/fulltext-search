package htw.ai.p2p.speechsearch.api.service

import cats.effect.Sync
import cats.effect.concurrent.Ref
import htw.ai.p2p.speechsearch.api.service.Indexes.IndexError
import htw.ai.p2p.speechsearch.domain.Index
import htw.ai.p2p.speechsearch.domain.model.Speech

import java.time.LocalDate

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
trait Indexes[F[_]] {
  def create(speech: Speech): F[Either[IndexError, F[Index]]]
}

object Indexes {
  def apply[F[_]](implicit ev: Indexes[F]): Indexes[F] = ev

  //noinspection ConvertExpressionToSAM
  def impl[F[_]: Sync](indexRef: Ref[F, Index]): Indexes[F] =
    new Indexes[F] {
      override def create(
          speech: Speech
      ): F[Either[IndexError, F[Index]]] = {
        val index = indexRef.updateAndGet(_.index(speech))
        Sync[F].pure(Right(index))
      }
    }

  sealed trait IndexError extends BaseError

  final case class UnknownError(
      speechDoc: Speech,
      message: String
  ) extends IndexError
}
