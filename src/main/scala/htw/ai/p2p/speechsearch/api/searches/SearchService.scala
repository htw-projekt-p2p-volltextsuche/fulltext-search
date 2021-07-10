package htw.ai.p2p.speechsearch.api.searches

import cats.Parallel
import cats.effect._
import cats.implicits._
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex.{PostingList, Term}
import htw.ai.p2p.speechsearch.domain.model.result.SearchResult
import htw.ai.p2p.speechsearch.domain.model.search.Search
import htw.ai.p2p.speechsearch.domain.{Searcher, Tokenizer}
import io.chrisdavenport.log4cats.Logger

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
trait SearchService[F[_]] {

  def create(search: Search): F[SearchResult]

}

object SearchService {

  type CachedIndex = Map[Term, PostingList]

  def apply[F[_]](implicit ev: SearchService[F]): SearchService[F] = ev

  def impl[F[_]: Sync: Parallel: Logger](
    searcher: Searcher,
    ii: InvertedIndex[F]
  ): SearchService[F] = new SearchService[F] {

    override def create(search: Search): F[SearchResult] =
      for {
        prefetched <- retrieveInvertedIndex(search, searcher.tokenizer)
        size       <- ii.size
      } yield searcher.search(search, prefetched, size)

    private def retrieveInvertedIndex(
      search: Search,
      tokenizer: Tokenizer
    ): F[CachedIndex] =
      ii.getAll(tokenizer.extractDistinctTokens(search))

  }

}
