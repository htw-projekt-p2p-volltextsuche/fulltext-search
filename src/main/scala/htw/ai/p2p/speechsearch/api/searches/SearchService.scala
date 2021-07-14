package htw.ai.p2p.speechsearch.api.searches

import cats.Parallel
import cats.data.OptionT
import cats.effect._
import cats.implicits._
import htw.ai.p2p.speechsearch.domain.core.invertedindex.InvertedIndex
import htw.ai.p2p.speechsearch.domain.core.invertedindex.InvertedIndex._
import htw.ai.p2p.speechsearch.domain.core.model.result.{ResultEntry, SearchResult}
import htw.ai.p2p.speechsearch.domain.core.model.search.Search
import htw.ai.p2p.speechsearch.domain.core.{BackgroundTask, Searcher, Tokenizer}
import htw.ai.p2p.speechsearch.domain.lru.{LruCache, LruRef}
import io.chrisdavenport.log4cats.Logger
import retry.Sleep

import java.time.{Instant, LocalDateTime, ZoneId}
import scala.concurrent.duration.{DurationInt, FiniteDuration, MILLISECONDS}

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
trait SearchService[F[_]] {

  def create(search: PaginatedSearch): F[SearchResult]

}

object SearchService {

  def apply[F[_]](implicit ev: SearchService[F]): SearchService[F] = ev

  type SearchCache = LruCache[Search, SearchResult]

  def impl[F[_]: Sync: Parallel: Concurrent: Timer: Logger](
    searcher: Searcher,
    ii: InvertedIndex[F],
    cacheRef: LruRef[F, Search, SearchResult]
  ): SearchService[F] = new SearchService[F] {

    override def create(search: PaginatedSearch): F[SearchResult] =
      OptionT(cacheRef.get(search.search))
        .getOrElseF(retrieveSearchResult(search.search))
        .map(_.paginate(search.pageInfo))

    private def retrieveSearchResult(search: Search): F[SearchResult] =
      for {
        prefetched <- ii getAll searcher.tokenizer.extractDistinctTokens(search)
        size       <- ii.size
        result      = searcher.search(search, prefetched, size)
        _          <- cacheRef.put(search, result)
      } yield searcher.search(search, prefetched, size)

  }

}
