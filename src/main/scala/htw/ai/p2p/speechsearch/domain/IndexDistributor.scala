package htw.ai.p2p.speechsearch.domain

import cats.effect.concurrent.Ref
import htw.ai.p2p.speechsearch.config.SpeechSearchConfig
import retry.Sleep
import cats.implicits._
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex.IndexMap

import scala.concurrent.duration.{FiniteDuration, MINUTES}

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
trait IndexDistributor[F[_]] {

  def run: F[Unit]

}

//object IndexDistributor {
//
//  def apply[F[_]](ev: IndexDistributor[F]): IndexDistributor[F] = ev
//
//  def impl[F[_]: Sleep](
//    ref: Ref[F, IndexMap],
//    config: SpeechSearchConfig
//  ): IndexDistributor[F] =
//    new IndexDistributor[F] {
//
//      override def run: F[Unit] = (
//        Sleep[F].sleep(FiniteDuration(5, MINUTES)) *> ref.getAndSet(Map.empty)
//      )
//    }
//
//}
