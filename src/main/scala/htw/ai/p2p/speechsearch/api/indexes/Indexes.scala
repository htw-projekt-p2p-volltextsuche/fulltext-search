package htw.ai.p2p.speechsearch.api.indexes

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import htw.ai.p2p.speechsearch.api.IndexError
import htw.ai.p2p.speechsearch.domain.Index
import htw.ai.p2p.speechsearch.domain.model.speech.Speech

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
trait Indexes[F[_]] {
  def create(speech: Speech): F[Either[IndexError, String]]
}

object Indexes {

  def apply[F[_]](implicit ev: Indexes[F]): Indexes[F] = ev

  def impl[F[_] : Sync](indexRef: Ref[F, Index]): Indexes[F] =
    speech =>
      for {
        success <- indexRef tryUpdate (_.index(speech))
        result = if (success) Right(createSuccessMessage(speech))
        else Left(createIndexError(speech))
      } yield result

  private def createSuccessMessage[F[_] : Sync](speech: Speech) =
    s"Speech with id ${speech.docId} was successfully indexed"

  private def createIndexError(speech: Speech) =
    IndexError(
      speech,
      s"Indexing the document with id '${speech.docId}' failed."
    )

}
