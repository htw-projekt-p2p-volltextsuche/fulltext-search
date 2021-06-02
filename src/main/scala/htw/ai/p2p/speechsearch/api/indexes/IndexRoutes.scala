package htw.ai.p2p.speechsearch.api.indexes

import cats.effect.Sync
import cats.implicits._
import htw.ai.p2p.speechsearch.api.indexes.Indexes.SpeechData
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
        speechData <- req.as[SpeechData]
        result <- indexes.create(speechData)
        resp <- result match {
          case Left(error) => BadRequest(error)
          case Right(i)    => Ok(i.map(_.postings("und").toString))
        }
      } yield resp
  }
}
