package htw.ai.p2p.speechsearch.domain

import cats.implicits.catsSyntaxSemigroup
import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestData.TestTokenizer
import htw.ai.p2p.speechsearch.TestUtils.{TestString, readSpeechFromFile}

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class IndexerSpec extends BaseShouldSpec {

  private val speech1 = readSpeechFromFile(
    "speech_carsten_schneider_23_04_2021.json"
  )
  private val speech2 = readSpeechFromFile("speech_olaf_scholz_23_04_2021.json")
  private val indexer = Indexer(TestTokenizer)

  "An IndexService" should "find a posting after indexing a speech" in {
    val indexMap = Indexer(TestTokenizer).index(speech1)

    val postingList = indexMap("Bundesfinanzminister".stemmed)
    postingList should have size 1
    postingList.head.tf shouldEqual 3
  }

  it should "find all postings after indexing multiple speeches" in {
    val indexMap = indexer.index(speech1) |+| indexer.index(speech2)

    indexMap("Mittwoch".stemmed)
      .map(_.tf)
      .sum shouldEqual 2
  }

}
