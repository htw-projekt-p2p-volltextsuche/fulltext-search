package htw.ai.p2p.speechsearch.domain.core.invertedindex

import cats.Parallel
import cats.effect.concurrent.Ref
import cats.effect.{Clock, Concurrent, Fiber, Sync, Timer}
import cats.implicits._
import htw.ai.p2p.speechsearch.api.peers.PeerClient
import htw.ai.p2p.speechsearch.domain.ImplicitUtilities.FormalizedString
import htw.ai.p2p.speechsearch.domain.core.BackgroundTask
import htw.ai.p2p.speechsearch.domain.core.invertedindex.InvertedIndex._
import io.chrisdavenport.log4cats.Logger
import retry.Sleep

import java.time.{Instant, LocalDateTime, ZoneId}
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class LazyDistributedInvertedIndex[
  F[_]: Sync: Concurrent: Parallel: Sleep: Logger: Timer
](
  indexRef: Ref[F, IndexMap],
  client: PeerClient[F],
  distributionInterval: FiniteDuration,
  distributionChunkSize: Int
) extends LocalInvertedIndex[F](indexRef)
    with BackgroundTask[F] {

  override def size: F[Int] = client.getIndexSize

  override def get(term: Term): F[(Term, PostingList)] =
    client.getPosting(term)

  override def getAll(terms: List[Term]): F[IndexMap] =
    client.getPostings(terms)

  override def run: F[Fiber[F, Unit]] =
    Concurrent[F].start {
      for {
        now    <- Clock[F].realTime(MILLISECONDS)
        inst    = Instant.ofEpochMilli(now + distributionInterval.toMillis)
        nextRun = LocalDateTime.ofInstant(inst, ZoneId.systemDefault())
        _ <-
          Logger[F].info(
            s"Scheduled next index distribution run to execute in ${distributionInterval.toSeconds} seconds (next run: $nextRun)."
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
    cachedIndex.grouped(distributionChunkSize).toSeq.parTraverse(publish) *> ()
      .pure[F]

  private def publish(cachedIndex: IndexMap): F[Unit] =
    for {
      _ <- Logger[F].info(
             s"Start to publish ${cachedIndex.size} entries to P2P network."
           )
      failed <- client.insert(cachedIndex)
      _ <- Logger[F].info(
             s"Executed index distribution. Insertion of ${failed.size} " +
               s"${"entry".formalized(failed.size)} failed."
           )
      _ <- insertAll(failed)
    } yield ()

}
