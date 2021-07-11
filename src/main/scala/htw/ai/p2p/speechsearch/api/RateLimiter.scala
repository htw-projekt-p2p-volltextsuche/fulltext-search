package htw.ai.p2p.speechsearch.api

import cats.Monad
import cats.effect.concurrent.Semaphore
import cats.effect.{Concurrent, ContextShift, Timer}
import cats.implicits._

import scala.concurrent.duration.DurationInt

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object RateLimiter {
//
//  def rateLimited[A, B, F[_]: Monad: Concurrent: ContextShift: Timer](
//    s: Semaphore[F],
//    f: A => F[B]
//  ): A => F[B] = { a: A =>
//    for {
//      _      <- s.acquire
//      timer  <- Timer[F].sleep(1.second) .start
//      result <- f(a)
//      _      <- timer.join
//      _      <- s.release
//    } yield result
//  }

}
