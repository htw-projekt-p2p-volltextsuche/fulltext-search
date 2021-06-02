package htw.ai.p2p.speechsearch.api.searches

import cats.effect.Sync
import cats.implicits._
import htw.ai.p2p.speechsearch.api.searches.Searches.QueryData
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
class SearchRoutes[F[_]: Sync](searches: Searches[F]) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "searches" =>
      for {
        search <- req.as[QueryData]
        result <- searches.create(search)
        resp <- result match {
          case Left(error)         => BadRequest(error)
          case Right(searchResult) => Ok(searchResult)
        }
      } yield resp
  }
}
