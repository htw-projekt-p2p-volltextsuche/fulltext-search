package htw.ai.p2p.speechsearch.api.index

import cats.effect._
import cats.implicits._
import htw.ai.p2p.speechsearch.domain.core.ImplicitUtilities.FormalizedString
import htw.ai.p2p.speechsearch.domain.core.Indexer
import htw.ai.p2p.speechsearch.domain.core.invertedindex.InvertedIndex
import htw.ai.p2p.speechsearch.domain.core.invertedindex.InvertedIndex.{
  IndexMap,
  Term
}
import htw.ai.p2p.speechsearch.domain.core.model.speech.{Posting, Speech}
import io.chrisdavenport.log4cats.Logger

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
trait IndexService[F[_]] {

  def insert(speech: Speech): F[IndexSuccess]

  def insert(speeches: List[Speech]): F[IndexSuccess]

}

object IndexService {

  def apply[F[_]](implicit ev: IndexService[F]): IndexService[F] = ev

  def impl[F[_]: Sync: Logger](
    indexer: Indexer,
    ii: InvertedIndex[F]
  ): IndexService[F] =
    new IndexService[F] {

      override def insert(speech: Speech): F[IndexSuccess] = {
        val indexEntries = indexer.index(speech)
        for {
          response <- indexAll(indexEntries, speech)
          _ <- Logger[F].info(
                 s"Successfully indexed single speech with doc_id '${speech.docId}'."
               )
        } yield response
      }

      override def insert(speeches: List[Speech]): F[IndexSuccess] = {
        val indexEntries = speeches.foldLeft(Map.empty[Term, List[Posting]]) {
          (cache, speech) => cache |+| indexer.index(speech)
        }
        for {
          response <- indexAll(indexEntries, speeches: _*)
          _ <-
            Logger[F].info(
              s"Successfully indexed ${speeches.size} ${"speech".formalize(speeches.size)}."
            )
        } yield response
      }

      private def indexAll(
        indexEntries: IndexMap,
        speeches: Speech*
      ): F[IndexSuccess] =
        (ii :++ indexEntries) *> IndexSuccess(
          s"${speeches.size} ${"speech".formalize(speeches.size)} have been successfully indexed: " +
            s"${speeches.map(_.docId.self).mkString(", ")}"
        ).pure[F]

    }

}
