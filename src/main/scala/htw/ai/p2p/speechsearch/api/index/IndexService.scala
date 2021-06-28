package htw.ai.p2p.speechsearch.api.index

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import htw.ai.p2p.speechsearch.api._
import htw.ai.p2p.speechsearch.domain.Index
import htw.ai.p2p.speechsearch.domain.model.speech.Speech

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
trait IndexService[F[_]] {

  def create(speech: Speech): F[Either[IndexError, Success]]

  def create(speeches: List[Speech]): F[Either[IndexError, Success]]

}

object IndexService {

  def apply[F[_]](implicit ev: IndexService[F]): IndexService[F] = ev

  def impl[F[_]: Sync](indexRef: Ref[F, Index]): IndexService[F] =
    new IndexService[F] {
      override def create(speech: Speech): F[Either[IndexError, Success]] =
        for {
          success <- indexRef tryUpdate (_.index(speech))
          result   = createResponse(success, speech)
        } yield result

      override def create(speeches: List[Speech]): F[Either[IndexError, Success]] =
        for {
          success <- indexRef tryUpdate ((i: Index) =>
                       speeches.foldLeft(i)((i, s) => i.index(s))
                     )
          result = createResponse(success, speeches: _*)
        } yield result
    }

  private def createResponse[F[_]: Sync](
    success: Boolean,
    speeches: Speech*
  ): Either[IndexError, Success] =
    if (success) Right(createSuccessMessage(speeches))
    else Left(createIndexError(speeches))

  private def createSuccessMessage(speeches: Seq[Speech]) =
    Success(
      s"Speeches with id's ${speeches map (_.docId)} have been successfully indexed"
    )

  private def createIndexError(speeches: Seq[Speech]) =
    IndexError(
      speeches,
      s"Indexing the speeches with id's ${speeches map (_.docId)} failed."
    )

}
