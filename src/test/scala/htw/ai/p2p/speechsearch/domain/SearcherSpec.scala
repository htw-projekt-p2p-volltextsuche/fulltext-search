package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestUtils.readSpeechFromFile

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
class SearcherSpec extends BaseShouldSpec {

  "A Searcher" should "find indexed terms" in {
    val speech = readSpeechFromFile("speech_carsten_schneider_23_04_2021.txt")
    val nextIndex = Index().index(speech)

    val result = Searcher(nextIndex).search("Beitrag zur Stabilisierung")

    val firstResult = result.results.head
    firstResult.docId shouldEqual speech.docId
    firstResult.score should not be 0
  }
}
