package htw.ai.p2p.speechsearch

import cats.effect.Sync
import cats.implicits._
import htw.ai.p2p.speechsearch.api.Searches
import htw.ai.p2p.speechsearch.api.Searches.Search
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object SpeechSearchRoutes {

  def searchRoutes[F[_]: Sync](searches: Searches[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case request @ POST -> Root / "searches" =>
        for {
          search <- request.as[Search]
          result <- searches.create(search)
          response <- result match {
            case Left(error)         => BadRequest(error)
            case Right(searchResult) => Ok(searchResult)
          }
        } yield response
    }
  }
}
