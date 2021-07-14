package htw.ai.p2p.speechsearch.api.index

import cats.effect.Sync
import cats.implicits._
import htw.ai.p2p.speechsearch.api.errors.{ApiError, HttpErrorHandler}
import htw.ai.p2p.speechsearch.domain.core.model.speech.Speech
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Allow

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class IndexRoutes[F[_]: Sync] private (indexService: IndexService[F])
    extends Http4sDsl[F] {

  private val Index = "index"

  private val routes: HttpRoutes[F] = HttpRoutes.of[F] {

    case req @ POST -> Root / Index / "speech" =>
      for {
        speech <- req.as[Speech]
        result <- indexService.insert(speech)
        resp   <- Ok(result)
      } yield resp

    case req @ POST -> Root / Index / "speeches" =>
      for {
        speeches <- req.as[List[Speech]]
        result   <- indexService.insert(speeches)
        resp     <- Ok(result)
      } yield resp

    case _ @_ -> Root / Index / "speech"   => MethodNotAllowed(Allow(POST))
    case _ @_ -> Root / Index / "speeches" => MethodNotAllowed(Allow(POST))
  }

}

object IndexRoutes {

  def routes[F[_]: Sync](indexService: IndexService[F])(implicit
    H: HttpErrorHandler[F, ApiError]
  ): HttpRoutes[F] =
    H.handle(new IndexRoutes[F](indexService).routes)

}
