package htw.ai.p2p.speechsearch.api

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestData.entireSearch
import htw.ai.p2p.speechsearch.TestUtils._
import htw.ai.p2p.speechsearch.domain.model.search.Search
import htw.ai.p2p.speechsearch.domain.model.speech._
import io.circe.jawn._
import io.circe.syntax.EncoderOps

import java.time.LocalDate

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class JsonCodecSpec extends BaseShouldSpec {

  "A Speech" should "decode to domain from JSON properly" in {
    val fileName = "speech_carsten_schneider_23_04_2021.json"

    val speech = readSpeechFromFile(fileName)

    assert(speech.docId.value === "1234")
  }

  it should "encode to JSON from domain and decode back to domain" in {
    val speech = Speech(
      docId = DocId("9876"),
      title = "Cool Speech",
      speaker = "Nelson Mandela",
      affiliation = "noborders",
      date = LocalDate.now(),
      text = "Blah Blah Bl..."
    )

    val decoded = decode[Speech](speech.asJson.toString)

    decoded.isRight shouldBe true
  }

  "A Search" should "decode to domain from JSON properly" in {
    val fileName = "valid_search.json"

    val search = readSearchFromFile(fileName)

    assert(search.query.terms === "hello")
  }

  it should "encode to JSON from domain and decode back to domain" in {
    val search = entireSearch

    val decoded = decode[Search](search.asJson.toString)

    decoded.isRight shouldBe true
  }
}
