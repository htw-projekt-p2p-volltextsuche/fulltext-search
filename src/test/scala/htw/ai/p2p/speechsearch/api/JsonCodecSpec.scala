package htw.ai.p2p.speechsearch.api

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestData._
import htw.ai.p2p.speechsearch.TestUtils._
import htw.ai.p2p.speechsearch.domain.model.search.Search
import htw.ai.p2p.speechsearch.domain.model.speech._
import io.circe.jawn._
import io.circe.syntax.EncoderOps
import org.scalatest.EitherValues._

import java.time.LocalDate
import java.util.UUID

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class JsonCodecSpec extends BaseShouldSpec {

  "A Speech" should "decode to domain from JSON properly" in {
    val fileName = "speech_carsten_schneider_23_04_2021.json"

    val speech = readSpeechFromFile(fileName)

    speech.docId.self shouldBe UUID.fromString(
      "e5b9957b-a94f-4e44-a8a9-291d9ed7c70d"
    )
  }

  it should "encode to JSON from domain and decode back to domain" in {
    val speech = Speech(
      docId = DocId(ValidUuid1),
      title = "Cool Speech",
      speaker = "Nelson Mandela",
      affiliation = "noborders",
      date = LocalDate.now(),
      text = "Blah Blah Bl..."
    )

    val decoded = decode[Speech](speech.asJson.toString)

    decoded.value.docId.leftSideValue shouldBe DocId(ValidUuid1)
  }

  "A Search" should "decode to domain from JSON properly" in {
    val fileName = "valid_search.json"

    val search = readSearchFromFile(fileName)

    assert(search.query.terms === "hello")
  }

  it should "encode to JSON from domain and decode back to domain" in {
    val search = EntireSearch

    val decoded = decode[Search](search.asJson.toString)

    decoded.isRight shouldBe true
  }
}
