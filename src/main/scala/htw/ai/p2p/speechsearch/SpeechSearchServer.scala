package htw.ai.p2p.speechsearch

import cats.effect.{ConcurrentEffect, ExitCode, Timer}
import cats.implicits.toSemigroupKOps
import fs2.Stream
import htw.ai.p2p.speechsearch.api.indexes.{IndexRoutes, Indexes}
import htw.ai.p2p.speechsearch.api.searches.{SearchRoutes, Searches}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object SpeechSearchServer {

  def stream[F[_]: ConcurrentEffect](
                                      port: Int,
                                      searchAlg: Searches[F],
                                      indexAlg: Indexes[F],
                                      apiPrefix: String
                                    )(implicit T: Timer[F]): Stream[F, ExitCode] = {
    val httpApp = Router(
      apiPrefix -> (
        new SearchRoutes[F](searchAlg).routes <+> new IndexRoutes[F](indexAlg).routes
      )
    ).orNotFound

    val appWithMiddleware =
      Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

    BlazeServerBuilder[F](global)
      .bindHttp(port, "0.0.0.0")
      .withHttpApp(appWithMiddleware)
      .serve
  }

}
