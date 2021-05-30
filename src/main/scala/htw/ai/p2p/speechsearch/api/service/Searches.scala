package htw.ai.p2p.speechsearch.api.service

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import htw.ai.p2p.speechsearch.api.service.Searches.{Search, SearchError}
import htw.ai.p2p.speechsearch.domain.model.SearchResult
import htw.ai.p2p.speechsearch.domain.{Index, Searcher}

/**
  * @author Joscha Seelig <jduesentrieb> 2021
 **/
trait Searches[F[_]] {
  def create(search: Search): F[Either[SearchError, SearchResult]]
}

object Searches {
  def apply[F[_]](implicit ev: Searches[F]): Searches[F] = ev

  //noinspection ConvertExpressionToSAM
  def impl[F[_]: Sync](indexRef: Ref[F, Index]): Searches[F] =
    new Searches[F] {
      override def create(
          search: Search
      ): F[Either[SearchError, SearchResult]] =
        for {
          index <- indexRef.get
          searchResult = Searcher(index).search(search.query)
        } yield Right(searchResult)
    }

  final case class Search(query: String)

  sealed trait SearchError extends BaseError

  final case class UnknownError(search: Search, message: String)
      extends SearchError
}
