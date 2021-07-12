package htw.ai.p2p.speechsearch.api.searches

import cats.effect.Sync
import cats.implicits._
import htw.ai.p2p.speechsearch.api.errors.{ApiError, HttpErrorHandler}
import htw.ai.p2p.speechsearch.domain.model.result.SearchResult._
import htw.ai.p2p.speechsearch.domain.model.search.Search
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Allow

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class SearchRoutes[F[_]: Sync](searchService: SearchService[F])
    extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "searches" =>
      for {
        search <- req.as[Search]
        result <- searchService.create(search)
        resp   <- Ok(result)
      } yield resp
    case _ @_ -> Root / "searches" => MethodNotAllowed(Allow(POST))
  }
}

object SearchRoutes {

  def routes[F[_]: Sync](searchService: SearchService[F])(implicit
    H: HttpErrorHandler[F, ApiError]
  ): HttpRoutes[F] =
    H.handle(new SearchRoutes[F](searchService).routes)

}
