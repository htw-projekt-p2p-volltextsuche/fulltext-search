package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestUtils.readSpeechFromFile

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class IndexSpec extends BaseShouldSpec {

  "An Index" should "find a posting after indexing a speech" in {
    val speech = readSpeechFromFile("speech_carsten_schneider_23_04_2021.json")

    val nextIndex = Index().index(speech)

    nextIndex
      .postings("Bundesfinanzminister")
      .head
      .tf shouldEqual 3
  }

  it should "find all postings after indexing multiple speeches" in {
    val speech1 = readSpeechFromFile("speech_carsten_schneider_23_04_2021.json")
    val speech2 = readSpeechFromFile("speech_olaf_scholz_23_04_2021.json")

    val nextIndex = Index().index(speech1).index(speech2)

    nextIndex
      .postings("Mittwoch")
      .map(_.tf)
      .sum shouldEqual 2
  }
}
