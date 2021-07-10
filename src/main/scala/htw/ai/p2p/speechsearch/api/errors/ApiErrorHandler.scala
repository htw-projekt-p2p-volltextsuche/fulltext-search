package htw.ai.p2p.speechsearch.api.errors

import cats.MonadError
import cats.implicits._
import com.olegpy.meow.hierarchy._
import io.chrisdavenport.log4cats.Logger
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class ApiErrorHandler[F[_]](implicit M: MonadError[F, ApiError], L: Logger[F])
    extends HttpErrorHandler[F, ApiError]
    with Http4sDsl[F] {

  private val handler: ApiError => F[Response[F]] = {
    case SearchError(search) =>
      InternalServerError(
        s"Search with id '${search.searchId}' failed."
      )
    case IndexError(speeches) =>
      InternalServerError(
        s"Indexing speeches with id's ${speeches.map(_.docId).mkString(",")} failed."
      )
    case PeerServerFailure(message) =>
      L.warn(
        s"Peer responded with unexpected status: $message"
      ) *> BadGateway(message)
    case PeerServerError(e) =>
      L.error(e)(
        s"Processing in P2P network failed."
      ) *> InternalServerError(e.getLocalizedMessage)
    case PeerConnectionError(e) =>
      L.error(e)(
        s"Failed to connect to the P2P network via entry point ${e.upstream}"
      ) *> BadGateway("Failed to connect to the P2P network.")
  }

  override def handle(routes: HttpRoutes[F]): HttpRoutes[F] =
    RoutesErrorHandler(routes)(handler)

}
