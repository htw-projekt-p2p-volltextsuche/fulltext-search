package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.domain.model.Posting

/**
  * @author Joscha Seelig <jduesentrieb> 2021
 **/
object LocalInvertedIndex {
  def apply(): InvertedIndex = new LocalInvertedIndex()
}

class LocalInvertedIndex private (
    store: Map[String, List[Posting]] = Map()
) extends InvertedIndex {

  override def insert(term: Term, posting: Posting): InvertedIndex =
    new LocalInvertedIndex(appendByKey(store, term -> posting))

  override def insertAll(entries: Map[Term, Posting]): InvertedIndex =
    new LocalInvertedIndex(
      store ++ entries.foldLeft(Map.empty[Term, PostingList]) {
        case (map, (term, postings)) => appendByKey(map, term -> postings)
      }
    )

  override def get(term: Term): PostingList = store getOrElse (term, List.empty)

  override def getAll(terms: List[Term]): Map[Term, PostingList] =
    terms.map(term => term -> store.getOrElse(term, Nil)).toMap

  private def appendByKey(
      map: Map[Term, PostingList],
      entry: (Term, Posting)
  ): Map[Term, PostingList] =
    map + (entry._1 -> (entry._2 :: store.getOrElse(entry._1, List.empty)))

  override def size: Int = store.size
}
