package htw.ai.p2p.speechsearch.domain.core

import cats.effect.Fiber

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
trait BackgroundTask[F[_]] {

  def run: F[Fiber[F, Unit]]

}
