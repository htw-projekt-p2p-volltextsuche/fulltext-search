package htw.ai.p2p.speechsearch.api

import cats.effect.IO
import cats.effect.concurrent.Ref
import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestUtils.readSpeechFromFile
import htw.ai.p2p.speechsearch.api.index.{IndexRoutes, IndexService}
import htw.ai.p2p.speechsearch.domain.invertedindex.LocalInvertedIndex
import htw.ai.p2p.speechsearch.domain.model.speech.Speech
import htw.ai.p2p.speechsearch.domain.{Index, Tokenizer}
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.implicits._
import org.http4s.circe.jsonEncoder

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class IndexServiceSpec extends BaseShouldSpec {

  "The Route /index/speech" should "return status code 200 indexing was successful" in {
    val speech =
      readSpeechFromFile("speech_carsten_schneider_23_04_2021.json")

    val response: Response[IO] = indexSpeech(speech)

    assert(
      response.status == Status.Ok,
      s"Expected '${response.status}' to be '${Status.Ok}'"
    )
  }

  it should "throw MalformedMessageBodyFailure if given speech is invalid" in {
    val invalidSpeech = """[{"speech":{"meaning of life":42}}]"""

    an[InvalidMessageBodyFailure] should be thrownBy {
      indexSpeechAsPlainText(invalidSpeech)
    }
  }

  "The route /index/speeches" should "process multiple speeches successfully" in {
    val speeches = List(
      readSpeechFromFile("speech_carsten_schneider_23_04_2021.json"),
      readSpeechFromFile("speech_olaf_scholz_23_04_2021.json")
    )

    val response = indexSpeeches(speeches)

    response.status shouldBe Status.Ok
  }

  private val server: HttpApp[IO] = {
    val index    = Index(Tokenizer(), LocalInvertedIndex())
    val indexRef = Ref[IO].of(index).unsafeRunSync()
    val indexes  = IndexService.impl(indexRef)
    new IndexRoutes(indexes).routes.orNotFound
  }

  private[this] def indexSpeech(speech: Speech): Response[IO] =
    indexSpeechAsPlainText(speech.asJson.toString)

  private[this] def indexSpeechAsPlainText(speechJson: String): Response[IO] = {
    val postSpeech =
      Request[IO](Method.POST, uri"/index/speech").withEntity(speechJson)
    this.server.run(postSpeech).unsafeRunSync()
  }

  private[this] def indexSpeeches(speeches: List[Speech]): Response[IO] = {
    val postSpeech =
      Request[IO](Method.POST, uri"/index/speeches").withEntity(speeches.asJson)
    this.server.run(postSpeech).unsafeRunSync()
  }

}
