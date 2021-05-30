package htw.ai.p2p.speechsearch.api

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestUtils.readSpeechFromFile
import htw.ai.p2p.speechsearch.domain.model.Speech
import io.circe.generic.auto._
import io.circe.jawn._
import io.circe.syntax.EncoderOps

import java.time.LocalDate

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
class JsonCodecSpec extends BaseShouldSpec {

  "A Speech" should "be decoded to domain object from JSON properly" in {
    val fileName = "speech_carsten_schneider_23_04_2021.txt"

    val speech = readSpeechFromFile(fileName)

    assert(speech.docId === "1234")
  }

  it should "be decoded from JSON that was encoded from domain objet" in {
    val speech = Speech(
      docId = "9876",
      title = "Cool Speech",
      speaker = "Nelson Mandela",
      affiliation = "noborders",
      date = LocalDate.now(),
      text = "Blah Blah Bl..."
    )

//    Request[IO].
    decode[Speech](speech.asJson.toString)
  }
}
