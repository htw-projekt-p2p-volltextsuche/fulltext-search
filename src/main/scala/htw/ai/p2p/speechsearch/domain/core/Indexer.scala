package htw.ai.p2p.speechsearch.domain.core

import htw.ai.p2p.speechsearch.domain.core.Tokenizer.buildFilterTerm
import htw.ai.p2p.speechsearch.domain.core.invertedindex.InvertedIndex.Term
import htw.ai.p2p.speechsearch.domain.core.model.search.FilterCriteria._
import htw.ai.p2p.speechsearch.domain.core.model.speech.{Posting, Speech}

/**
 * Main unit for the management of the inverted index.
 * It handles the creation, updating as well as the readout of the inverted index.
 *
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class Indexer(tokenizer: Tokenizer) {

  def index(speech: Speech): Map[Term, List[Posting]] = {
    val terms    = tokenizer(speech.title) ::: tokenizer(speech.text)
    val docLen   = terms.size
    val postings = buildPostings(speech, terms, docLen)
    val filters  = buildFilters(speech, docLen)

    postings ++ filters
  }

  private def buildFilters(
    speech: Speech,
    docLen: Int
  ): Map[String, List[Posting]] = {
    val filterPosting = Posting(speech.docId, 0, docLen)
    Map(
      buildFilterTerm(Speaker, speech.speaker)         -> List(filterPosting),
      buildFilterTerm(Affiliation, speech.affiliation) -> List(filterPosting)
    )
  }

  private def buildPostings(
    speech: Speech,
    terms: List[String],
    docLen: Int
  ): Map[String, List[Posting]] =
    for {
      (term, tf) <- terms.groupMapReduce(identity)(_ => 1)(_ + _)
    } yield term -> List(Posting(speech.docId, tf, docLen))

}

object Indexer {

  def apply(tokenizer: Tokenizer) = new Indexer(tokenizer)

}
