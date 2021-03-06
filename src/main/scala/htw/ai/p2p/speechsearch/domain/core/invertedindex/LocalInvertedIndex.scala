package htw.ai.p2p.speechsearch.domain.core.invertedindex

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import InvertedIndex._
import LocalInvertedIndex.AppendableMap

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class LocalInvertedIndex[F[_]: Sync](indexRef: Ref[F, IndexMap])
    extends InvertedIndex[F] {

  override def size: F[Int] = indexRef.get.map(index => index.size)

  override def get(term: Term): F[(Term, PostingList)] =
    for {
      index: IndexMap <- indexRef.get
      postings         = index getOrElse (term, Nil)
    } yield term -> postings

  override def getAll(terms: List[Term]): F[IndexMap] = for {
    index   <- indexRef.get
    postings = terms.map(term => term -> index.getOrElse(term, Nil)).toMap
  } yield postings

  override def insert(term: Term, postings: PostingList): F[Unit] =
    indexRef update (_ appendByKey (term -> postings))

  override def insertAll(entries: IndexMap): F[Unit] =
    indexRef update {
      entries.foldLeft(_) { case (ii, (term, postings)) =>
        ii appendByKey (term -> postings)
      }
    }

}

object LocalInvertedIndex {

  implicit class AppendableMap[K, V](self: Map[K, List[V]]) {

    def appendByKey(entry: (K, List[V])): Map[K, List[V]] =
      entry match {
        case (term, postings) =>
          self + (term -> (postings ::: self.getOrElse(term, Nil)))
      }

  }

}
