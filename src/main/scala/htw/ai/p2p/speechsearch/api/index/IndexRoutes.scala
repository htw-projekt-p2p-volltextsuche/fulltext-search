package htw.ai.p2p.speechsearch.api.index

import cats.effect.Sync
import cats.implicits._
import htw.ai.p2p.speechsearch.api.{IndexError, Success}
import htw.ai.p2p.speechsearch.domain.model.speech.Speech
import io.circe.generic.auto._
import org.http4s.{HttpRoutes, Response}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class IndexRoutes[F[_]: Sync](indexes: IndexService[F]) extends Http4sDsl[F] {

  private val Index = "index"

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / Index / "speech" =>
      for {
        speech <- req.as[Speech]
        result <- indexes.create(speech)
        resp   <- handleResult(result)
      } yield resp
    case req @ POST -> Root / Index / "speeches" =>
      for {
        speeches <- req.as[List[Speech]]
        result   <- indexes.create(speeches)
        resp     <- handleResult(result)
      } yield resp
  }

  private val handleResult: Either[IndexError, Success] => F[Response[F]] = {
    case Left(error) => BadRequest(error)
    case Right(_)    => Ok()
  }

}
