package htw.ai.p2p.speechsearch.domain.core.invertedindex

import cats.effect.Sync
import cats.implicits._
import htw.ai.p2p.speechsearch.api.peers.PeerClient
import InvertedIndex._
import io.chrisdavenport.log4cats.Logger

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class DistributedInvertedIndex[F[_]: Sync: Logger](client: PeerClient[F])
    extends InvertedIndex[F] {

  override def size: F[Int] = client.getIndexSize

  override def insert(term: Term, postings: PostingList): F[Unit] =
    for {
      _ <- client.insert(term, postings)
      _ <- Logger[F].info(s"Insertion of posting for term $term succeeded.")
    } yield ()

  override def insertAll(entries: IndexMap): F[Unit] =
    for {
      _ <-
        Logger[F].info(
          s"Attempting to insert ${entries.size} posting lists into the P2P network."
        )
      _ <- client.insert(entries)
      _ <- Logger[F].info(s"Insertion of posting lists succeeded.")
    } yield ()

  override def get(term: Term): F[(Term, PostingList)] =
    client.getPosting(term)

  override def getAll(terms: List[Term]): F[IndexMap] =
    client.getPostings(terms)

}
