package htw.ai.p2p.speechsearch.api.index

import cats.effect._
import cats.implicits._
import htw.ai.p2p.speechsearch.api._
import htw.ai.p2p.speechsearch.api.errors.IndexError
import htw.ai.p2p.speechsearch.domain.Indexer
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex.{IndexMap, Term}
import htw.ai.p2p.speechsearch.domain.model.speech.{Posting, Speech}
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
      private val F = implicitly[Sync[F]]

      override def insert(speech: Speech): F[IndexSuccess] = {
        val indexEntries = indexer.index(speech)
        for {
          response <- indexAll(indexEntries, speech)
          _ <-
            Logger[F].info(
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
          _        <- Logger[F].info(s"Successfully indexed ${speeches.size} speeches.")
        } yield response
      }

      private def indexAll(
        indexEntries: IndexMap,
        speeches: Speech*
      ): F[IndexSuccess] =
        for {
          success <- ii :++ indexEntries
          response <- if (success) createSuccessMessage(speeches).pure[F]
                      else F.raiseError(IndexError(speeches))
        } yield response

      private def createSuccessMessage(speeches: Seq[Speech]) =
        IndexSuccess(
          s"${speeches.size} speeches have been successfully indexed: ${speeches.map(_.docId.self).mkString(",")}"
        )
    }

}
