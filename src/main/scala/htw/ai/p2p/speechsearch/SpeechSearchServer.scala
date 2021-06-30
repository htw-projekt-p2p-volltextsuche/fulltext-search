package htw.ai.p2p.speechsearch

import cats.effect._
import cats.implicits._
import fs2.Stream
import htw.ai.p2p.speechsearch.api.index._
import htw.ai.p2p.speechsearch.api.searches._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object SpeechSearchServer {

  private val corsConfig = CORSConfig(
    anyOrigin = true,
    allowCredentials = true,
    maxAge = 1.day.toSeconds,
    anyMethod = false,
    allowedMethods = Set("POST").some
  )

  def stream[F[_]: ConcurrentEffect](
    port: Int,
    searchService: SearchService[F],
    indexService: IndexService[F],
    apiPrefix: String
  )(implicit T: Timer[F]): Stream[F, ExitCode] = {
    val httpService = Router(
      apiPrefix -> (
        new SearchRoutes[F](searchService).routes
          <+> new IndexRoutes[F](indexService).routes
      )
    ).orNotFound

    val serviceWithLogging =
      Logger.httpApp(logHeaders = true, logBody = true)(httpService)

    val corsService = CORS(serviceWithLogging, corsConfig)

    BlazeServerBuilder[F](global)
      .bindHttp(port, "0.0.0.0")
      .withHttpApp(corsService)
      .serve
  }

}
