package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.domain.model.{ResultEntry, SearchResult}

/**
  * @author Joscha Seelig <jduesentrieb> 2021
 **/
class Searcher(index: Index) {

  // TODO: postings should be retrieved as batch
  def search(query: String, topK: Int = 10): SearchResult = {
    val postings = for {
      term <- index.tokenizer(query)
      posting <- index.postings(term)
    } yield posting.docId -> posting.tf * math.pow(idf(term).getOrElse(0), 2)

    val results = postings
      .groupMapReduce(_._1)(_._2)(_ + _)
      .toSeq
      .sortBy(-_._2)
      .take(topK)
      .map { case (term, score) => ResultEntry(term, score) }

    SearchResult(results)
  }

  def idf(term: String): Option[Double] =
    index.docCount(term) match {
      case 0        => None
      case docCount => Some(math.log(index.size.toDouble / docCount.toDouble))
    }
}

object Searcher {
  def apply(index: Index) = new Searcher(index)
}
