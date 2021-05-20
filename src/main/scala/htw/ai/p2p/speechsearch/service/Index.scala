package htw.ai.p2p.speechsearch.service

import htw.ai.p2p.speechsearch.model.{Posting, Speech}

/**
  * Main unit for the management of the inverted index.
  * It handles the creation, updating as well as the readout of the inverted index.…
  *
  * @author Joscha Seelig <jduesentrieb> 2021
 **/
class Index(
    private val tokenizer: Tokenizer,
    private val invertedIndex: InvertedIndex
) {
  def index(speech: Speech): Index = {
    val postings: Map[String, Posting] = tokenizer
      .tokenize(speech.content)
      .groupBy(identity)
      .view
      .mapValues(_.size)
      .map { case (term, tf) => term -> Posting(speech.docId, tf) }
      .toMap
      .+("_speaker:" + speech.speaker -> Posting(speech.docId, 0))
      .+("_affiliation:" + speech.affiliation -> Posting(speech.docId, 0))

    new Index(tokenizer, invertedIndex :++ postings)
  }
}
