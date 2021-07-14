package htw.ai.p2p.speechsearch.api.index

import cats.effect.IO
import cats.effect.concurrent.Ref
import com.olegpy.meow.hierarchy._
import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.SpeechSearchServer.unsafeLogger
import htw.ai.p2p.speechsearch.TestData.TestTokenizer
import htw.ai.p2p.speechsearch.TestUtils.readSpeechFromFile
import htw.ai.p2p.speechsearch.api.errors._
import htw.ai.p2p.speechsearch.domain.core.Indexer
import htw.ai.p2p.speechsearch.domain.core.invertedindex.InvertedIndex
import htw.ai.p2p.speechsearch.domain.core.invertedindex.InvertedIndex._
import htw.ai.p2p.speechsearch.domain.core.model.speech.Speech
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.circe.jsonEncoder
import org.http4s.implicits._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class IndexRoutesSpec extends BaseShouldSpec {

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

  private val server: IO[HttpApp[IO]] = {
    implicit val eh: HttpErrorHandler[IO, ApiError] = new ApiErrorHandler[IO]
    for {
      indexRef <- Ref[IO].of(Map.empty[Term, PostingList])
      ii        = InvertedIndex.local[IO](indexRef)
      indexer   = Indexer(TestTokenizer)
      service   = IndexService.impl[IO](indexer, ii)
    } yield IndexRoutes.routes(service).orNotFound
  }

  private[this] def indexSpeech(speech: Speech): Response[IO] =
    indexSpeechAsPlainText(speech.asJson.toString)

  private[this] def indexSpeechAsPlainText(speechJson: String): Response[IO] = {
    val postSpeech =
      Request[IO](Method.POST, uri"/index/speech").withEntity(speechJson)
    this.server.flatMap(_.run(postSpeech)).unsafeRunSync()
  }

  private[this] def indexSpeeches(speeches: List[Speech]): Response[IO] = {
    val postSpeech =
      Request[IO](Method.POST, uri"/index/speeches").withEntity(speeches.asJson)
    this.server.flatMap(_.run(postSpeech)).unsafeRunSync()
  }

}
