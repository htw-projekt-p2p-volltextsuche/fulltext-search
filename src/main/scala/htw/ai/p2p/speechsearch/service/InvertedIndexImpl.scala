package htw.ai.p2p.speechsearch.service

import htw.ai.p2p.speechsearch.model.Posting

object InvertedIndexImpl {
  def apply(): InvertedIndex = new InvertedIndexImpl()
}

class InvertedIndexImpl private (
    store: Map[String, List[Posting]] = Map()
) extends InvertedIndex {

  override def insert(term: Term, postings: PostingList): InvertedIndex =
    new InvertedIndexImpl(
      appendByKey(store, term -> postings)
    )

  override def insertAll(entries: Map[Term, PostingList]): InvertedIndex =
    new InvertedIndexImpl(
      store ++ entries.foldLeft(Map[Term, PostingList]()) {
        case (map, (term, postings)) => appendByKey(map, term -> postings)
      }
    )

  override def get(term: Term): Option[PostingList] = store get term

  override def getAll(terms: List[Term]): Map[Term, PostingList] =
    terms.foldLeft(Map[Term, PostingList]()) { (map, term) =>
      store get term match {
        case Some(postings) => appendByKey(map, term -> postings)
        case None           => map
      }
    }

  private def appendByKey(
      map: Map[Term, PostingList],
      entry: (Term, PostingList)
  ): Map[Term, PostingList] =
    map + (entry._1 -> (entry._2 ::: store.getOrElse(entry._1, List.empty)))
}
