package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestUtils.readSpeechFromFile
import htw.ai.p2p.speechsearch.domain.model.{FullText, SearchQuery}

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
class SearcherSpec extends BaseShouldSpec {

  "A Searcher" should "find indexed terms" in {
    val speech = readSpeechFromFile("speech_carsten_schneider_23_04_2021.txt")
    val nextIndex = Index().index(speech)

    val result = Searcher(nextIndex).search(
      createSearchQuery("Beitrag zur Stabilisierung")
    )

    val firstResult = result.results.head
    firstResult.docId shouldEqual speech.docId
    firstResult.score should not be 0
  }

  private def createSearchQuery(query: String) =
    SearchQuery(
      maxResults = 15,
      searchType = FullText,
      terms = query
    )
}
