package htw.ai.p2p.speechsearch

import cats.effect.{ConcurrentEffect, Timer}
import fs2.Stream
import htw.ai.p2p.speechsearch.api.Searches
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object SpeechSearchServer {

  def stream[F[_]: ConcurrentEffect](implicit
      T: Timer[F]
  ): Stream[F, Nothing] = {
    val searchAlg = Searches.impl[F]

    val httpApp = (
      SpeechSearchRoutes.searchRoutes[F](searchAlg)
    ).orNotFound

    // Middleware
    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    for {
      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
