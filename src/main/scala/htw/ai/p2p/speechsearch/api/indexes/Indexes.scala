package htw.ai.p2p.speechsearch.api.indexes

import cats.effect.Sync
import cats.effect.concurrent.Ref
import htw.ai.p2p.speechsearch.api.IndexError
import htw.ai.p2p.speechsearch.domain.Index

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
trait Indexes[F[_]] {
  def create(speech: SpeechData): F[Either[IndexError, F[Index]]]
}

object Indexes {
  def apply[F[_]](implicit ev: Indexes[F]): Indexes[F] = ev

  //noinspection ConvertExpressionToSAM
  def impl[F[_]: Sync](indexRef: Ref[F, Index]): Indexes[F] =
    new Indexes[F] {
      override def create(
          speech: SpeechData
      ): F[Either[IndexError, F[Index]]] = {
        val index = indexRef.updateAndGet(_.index(speech.toDomain))
        Sync[F].pure(Right(index))
      }
    }
}
