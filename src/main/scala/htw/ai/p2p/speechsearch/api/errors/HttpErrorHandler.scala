package htw.ai.p2p.speechsearch.api.errors

import cats.ApplicativeError
import cats.data.{Kleisli, OptionT}
import cats.syntax.all._
import com.olegpy.meow.hierarchy._
import org.http4s.{HttpRoutes, Response}

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
trait HttpErrorHandler[F[_], E <: Throwable] {

  def handle(routes: HttpRoutes[F]): HttpRoutes[F]

}

object HttpErrorHandler {

  def apply[F[_], E <: Throwable](implicit
    ev: HttpErrorHandler[F, E]
  ): HttpErrorHandler[F, E] = ev

}

object RoutesErrorHandler {

  def apply[F[_]: ApplicativeError[*[_], E], E <: Throwable](routes: HttpRoutes[F])(
    handler: E => F[Response[F]]
  ): HttpRoutes[F] = Kleisli { req =>
    OptionT {
      routes.run(req).value.handleErrorWith(e => handler(e).map(Option(_)))
    }
  }

}
