package htw.ai.p2p.speechsearch.domain.invertedindex

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Fiber, Sync}
import cats.implicits._
import htw.ai.p2p.speechsearch.api.peers.PeerClient
import htw.ai.p2p.speechsearch.domain.ImplicitUtilities.FormalizedString
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex._
import io.chrisdavenport.log4cats.Logger
import retry.Sleep

import scala.concurrent.duration.FiniteDuration

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class LazyDistributedInvertedIndex[F[_]: Sync: Concurrent: Sleep: Logger](
  indexRef: Ref[F, IndexMap],
  client: PeerClient[F],
  distributionInterval: FiniteDuration
) extends LocalInvertedIndex[F](indexRef)
    with IndexDistributor[F] {

  override def size: F[Int] =
    for {
      json <- client.getRaw(IndexSizeKey)
      size <- json.as[Int].liftTo[F]
    } yield size

  override def get(term: Term): F[(Term, PostingList)] =
    client.getPosting(term)

  override def getAll(terms: List[Term]): F[IndexMap] =
    client.getPostings(terms)

  override def run: F[Fiber[F, Unit]] =
    Concurrent[F].start {
      for {
        _ <-
          Logger[F].info(
            s"Scheduled next index distribution run to execute in ${distributionInterval.toMinutes} minutes."
          )
        _     <- Sleep[F].sleep(distributionInterval)
        cache <- indexRef.getAndSet(Map.empty)
        _ <- if (cache.nonEmpty)
               Concurrent[F].start(distributeIndex(cache))
             else
               Logger[F].info(s"Cached index is empty - index distribution skipped.")
        _ <- run
      } yield ()
    }

  private def distributeIndex(cachedIndex: IndexMap): F[Unit] =
    for {
      _ <- Logger[F].info(
             s"Start to distribute ${cachedIndex.size} entries to P2P network."
           )
      failed <- client.insert(cachedIndex)
      _ <- Logger[F].info(
             s"Executed index distribution. Insertion of ${failed.size} " +
               s"${"entry".formalize(failed.size)} failed."
           )
      _ <- insertAll(failed)
    } yield ()

}