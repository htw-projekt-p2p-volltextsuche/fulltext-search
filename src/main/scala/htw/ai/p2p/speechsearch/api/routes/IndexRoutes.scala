package htw.ai.p2p.speechsearch.api.routes

import cats.effect.Sync
import cats.implicits._
import htw.ai.p2p.speechsearch.api.service.Indexes
import htw.ai.p2p.speechsearch.domain.model.Speech
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
class IndexRoutes[F[_]: Sync](indexes: Indexes[F]) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "indexes" =>
      for {
        speechDoc <- req.as[Speech]
        result <- indexes.create(speechDoc)
        resp <- result match {
          case Left(error) => BadRequest(error)
          case Right(i)    => Ok(i.map(_.postings("und").toString))
        }
      } yield resp
  }
}
