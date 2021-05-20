package htw.ai.p2p.speechsearch.api

import cats.effect.Sync
import htw.ai.p2p.speechsearch.api.Searches.{Search, SearchError, SearchResult}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
trait Searches[F[_]] {
  def create(search: Search): F[Either[SearchError, SearchResult]]
}

object Searches {
  def apply[F[_]](implicit ev: Searches[F]): Searches[F] = ev

  //noinspection ConvertExpressionToSAM
  def impl[F[_]: Sync]: Searches[F] =
    new Searches[F] {
      override def create(
          search: Search
      ): F[Either[SearchError, SearchResult]] = {
        val result = SearchResult(Nil)
        Sync[F].pure(Right(result))
      }
    }

  final case class Search(query: String)

  object Search {
    implicit val encoder: Encoder[Search] = deriveEncoder
    implicit def entityEncoder[F[_]: Sync]: EntityEncoder[F, Search] =
      jsonEncoderOf
    implicit val decoder: Decoder[Search] = deriveDecoder
    implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, Search] =
      jsonOf
  }

  final case class SearchResult(postings: List[String])

  object SearchResult {
    implicit val encoder: Encoder[SearchResult] = deriveEncoder
    implicit def entityEncoder[F[_]: Sync]: EntityEncoder[F, SearchResult] =
      jsonEncoderOf
    implicit val decoder: Decoder[SearchResult] = deriveDecoder
    implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, SearchResult] =
      jsonOf
  }

  sealed trait SearchError

  final case class UnknownError(search: Search, message: String)
      extends SearchError

  object SearchError {
    implicit val encoder: Encoder[SearchError] = deriveEncoder
    implicit def entityEncoder[F[_]: Sync]: EntityEncoder[F, SearchError] =
      jsonEncoderOf
    implicit val decoder: Decoder[SearchError] = deriveDecoder
    implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, SearchError] =
      jsonOf
  }
}
