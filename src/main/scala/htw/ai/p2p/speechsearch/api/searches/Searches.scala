package htw.ai.p2p.speechsearch.api.searches

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import htw.ai.p2p.speechsearch.api.searches.Searches.SearchError
import htw.ai.p2p.speechsearch.domain.model._
import htw.ai.p2p.speechsearch.domain.{Index, Searcher}

/**
  * @author Joscha Seelig <jduesentrieb> 2021
 **/
trait Searches[F[_]] {
  def create(search: QueryData): F[Either[SearchError, SearchResult]]
}

object Searches {
  def apply[F[_]](implicit ev: Searches[F]): Searches[F] = ev

  //noinspection ConvertExpressionToSAM
  def impl[F[_]: Sync](indexRef: Ref[F, Index]): Searches[F] =
    new Searches[F] {
      override def create(
          search: QueryData
      ): F[Either[SearchError, SearchResult]] =
        for {
          index <- indexRef.get
          searchResult = Searcher(index).search(search.toDomain)
        } yield Right(searchResult)
    }

  sealed trait SearchError

  final case class UnknownError(search: QueryData, message: String)
      extends SearchError
}
