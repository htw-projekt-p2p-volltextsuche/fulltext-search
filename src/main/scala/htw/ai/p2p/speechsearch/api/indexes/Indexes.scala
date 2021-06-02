package htw.ai.p2p.speechsearch.api.indexes

import cats.effect.Sync
import cats.effect.concurrent.Ref
import htw.ai.p2p.speechsearch.api.indexes.Indexes.{IndexError, SpeechData}
import htw.ai.p2p.speechsearch.domain.Index
import htw.ai.p2p.speechsearch.domain.model.{DocId, Speech}

import java.time.LocalDate

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

  final case class SpeechData(
      docId: String,
      title: String,
      speaker: String,
      affiliation: String,
      date: String,
      text: String
  ) {
    def toDomain: Speech =
      Speech(
        docId = DocId(docId),
        title = title,
        speaker = speaker,
        affiliation = affiliation,
        date = LocalDate.parse(date),
        text = text
      )
  }

  sealed trait IndexError

  final case class UnknownError(
      speechDoc: Speech,
      message: String
  ) extends IndexError
}
