package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.domain.model.{Posting, Speech}

/**
  * Main unit for the management of the inverted index.
  * It handles the creation, updating as well as the readout of the inverted index.…
  *
  * @author Joscha Seelig <jduesentrieb> 2021
 **/
object Index {
  def apply(): Index = Index(Tokenizer(), LocalInvertedIndex())
  def apply(t: Tokenizer, ii: InvertedIndex) = new Index(t, ii)
}

class Index(
    val tokenizer: Tokenizer,
    private val ii: InvertedIndex
) {

  private val SpeakerPrefix = "_speaker:"
  private val AffiliationPrefix = "_affiliation:"

  def index(speech: Speech): Index = {
    val postings: Map[String, Posting] =
      tokenizer(speech.text)
        .groupMapReduce(identity)(_ => 1)(_ + _)
        .map { case (term, tf) => term -> Posting(speech.docId, tf) }
        .+(SpeakerPrefix + speech.speaker -> Posting(speech.docId, 0))
        .+(AffiliationPrefix + speech.affiliation -> Posting(speech.docId, 0))

    new Index(tokenizer, ii :++ postings)
  }

  def postings(term: String): List[Posting] = ii(term.toLowerCase)

  def docCount(term: String): Int =
    postings(term).size // TODO: this needs to be optimized

  def size: Int = ii.size
}
