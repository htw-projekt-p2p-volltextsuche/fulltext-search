package htw.ai.p2p.speechsearch.api.searches

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import htw.ai.p2p.speechsearch.api.SearchError
import htw.ai.p2p.speechsearch.domain._
import htw.ai.p2p.speechsearch.domain.model.result.SearchResult
import htw.ai.p2p.speechsearch.domain.model.search.Search

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
trait SearchService[F[_]] {
  def create(search: Search): F[Either[SearchError, SearchResult]]
}

object SearchService {
  def apply[F[_]](implicit ev: SearchService[F]): SearchService[F] = ev

  def impl[F[_]: Sync](indexRef: Ref[F, Index]): SearchService[F] =
    search =>
      for {
        index       <- indexRef.get
        searchResult = Searcher(index).search(search)
      } yield Right(searchResult)
}
