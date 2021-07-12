package htw.ai.p2p.speechsearch.domain.invertedindex

import cats.effect.Fiber

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
trait IndexDistributor[F[_]] {

  def run: F[Fiber[F, Unit]]

}
