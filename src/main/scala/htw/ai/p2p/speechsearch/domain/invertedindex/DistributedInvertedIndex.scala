package htw.ai.p2p.speechsearch.domain.invertedindex

import cats.effect.Sync
import cats.implicits._
import htw.ai.p2p.speechsearch.api.peers.PeerClient
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex._
import io.chrisdavenport.log4cats.Logger

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class DistributedInvertedIndex[F[_]: Sync: Logger](client: PeerClient[F])
    extends InvertedIndex[F] {

  private val IndexSizeKey = "_keyset_size"

  override def size: F[Int] =
    for {
      json <- client.getRaw(IndexSizeKey)
      size <- json.as[Int].liftTo[F]
    } yield size

  override def insert(term: Term, postings: PostingList): F[Boolean] =
    for {
      success <- client.insert(term, postings)
      _       <- Logger[F].info(s"Insertion of posting for term $term succeeded: $success")
    } yield success

  override def insertAll(entries: IndexMap): F[Boolean] =
    for {
      _ <-
        Logger[F].info(
          s"Attempting to insert ${entries.size} posting lists into the P2P network."
        )
      success <- client.insert(entries)
      _       <- Logger[F].info(s"Insertion of posting lists succeeded: $success")
    } yield success

  override def get(term: Term): F[(Term, PostingList)] =
    client.getPosting(term)

  override def getAll(terms: List[Term]): F[IndexMap] =
    client.getPostings(terms)

}
