package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.domain.Tokenizer.buildFilterTerm
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex._
import htw.ai.p2p.speechsearch.domain.invertedindex._
import htw.ai.p2p.speechsearch.domain.model.search.FilterCriteria._
import htw.ai.p2p.speechsearch.domain.model.speech._

/**
 * Main unit for the management of the inverted index.
 * It handles the creation, updating as well as the readout of the inverted index.
 *
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class Index(
  val tokenizer: Tokenizer,
  private val ii: InvertedIndex
) {

  def index(speech: Speech): Index = {
    val terms    = tokenizer(speech.title) ::: tokenizer(speech.text)
    val docLen   = terms.size
    val postings = buildPostings(speech, terms, docLen)
    val filters  = buildFilters(speech, docLen)

    new Index(tokenizer, ii :++ postings ++ filters)
  }

  def postings(term: Term): PostingList = ii(term.toLowerCase)

  def postings(terms: List[Term]): Map[Term, PostingList] =
    ii getAll (terms map (_.toLowerCase))

  def size: Int = ii.size // TODO: optimize

  private def buildFilters(speech: Speech, docLen: Int): Map[String, Posting] = {
    val filterPosting = Posting(speech.docId, 0, docLen)
    Map(
      buildFilterTerm(Speaker, speech.speaker)         -> filterPosting,
      buildFilterTerm(Affiliation, speech.affiliation) -> filterPosting
    )
  }

  private def buildPostings(
    speech: Speech,
    terms: List[String],
    docLen: Int
  ): Map[String, Posting] =
    for {
      (term, tf) <- terms.groupMapReduce(identity)(_ => 1)(_ + _)
    } yield term -> Posting(speech.docId, tf, docLen)

}

object Index {

  def apply(): Index = Index(
    Tokenizer(),
    DistributedInvertedIndex(new DHTClientProduction())
  )

  def apply(t: Tokenizer, ii: InvertedIndex) = new Index(t, ii)

}
