package htw.ai.p2p.speechsearch.api.searches

import cats.effect.Sync
import cats.implicits._
import htw.ai.p2p.speechsearch.api.errors.{ApiError, HttpErrorHandler}
import htw.ai.p2p.speechsearch.domain.ImplicitUtilities._
import htw.ai.p2p.speechsearch.domain.core.model.result.SearchResult._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Allow

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class SearchRoutes[F[_]: Sync] private (searchService: SearchService[F])
    extends Http4sDsl[F] {

  private val Searches = "searches"

  private val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / Searches =>
      for {
        search <- req.as[PaginatedSearch]
        result <- searchService.create(search)
        resp   <- Ok(result)
      } yield resp
    case GET -> Root / Searches / "cache" / "evict" =>
      searchService.evictCache >>= (e =>
        Ok(s"Removed ${e.size} ${"element".formalized(e.size)} from the cache.")
      )
    case _ @_ -> Root / Searches => MethodNotAllowed(Allow(POST))
  }
}

object SearchRoutes {

  def routes[F[_]: Sync](searchService: SearchService[F])(implicit
    H: HttpErrorHandler[F, ApiError]
  ): HttpRoutes[F] =
    H.handle(new SearchRoutes[F](searchService).routes)

}
