package htw.ai.p2p.speechsearch.api

import cats.effect.IO
import cats.effect.concurrent.Ref
import htw.ai.p2p.speechsearch.TestUtils.readSpeechDataFromFile
import htw.ai.p2p.speechsearch.api.indexes.{IndexRoutes, Indexes, SpeechData}
import htw.ai.p2p.speechsearch.domain.invertedindex.LocalInvertedIndex
import htw.ai.p2p.speechsearch.domain.{Index, Tokenizer}
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.implicits._

/**
  * @author Joscha Seelig <jduesentrieb> 2021
 **/
class IndexesSpec extends CatsEffectSuite {

  test("should return status code 200 after successful indexing") {
    val speechData =
      readSpeechDataFromFile("speech_carsten_schneider_23_04_2021.txt")

    val response: Response[IO] = indexSpeech(speechData)

    assert(
      response.status == Status.Ok,
      s"Expected '${response.status}' to be '${Status.Ok}'"
    )
  }


  test("should return status code 400 if given speech is invalid") {
    val invalidSpeech =
      """{
        |"query": {
        | "terms": "what is the meaning of life?"
        |}""".stripMargin

    val response = indexSpeechAsPlainText(invalidSpeech)

    assert(response.status == Status.BadRequest)
  }

  private val server: HttpApp[IO] = {
    val index = Index(Tokenizer(), LocalInvertedIndex())
    val indexRef = Ref[IO].of(index).unsafeRunSync
    val indexes = Indexes.impl(indexRef)
    new IndexRoutes(indexes).routes.orNotFound
  }

  private[this] def indexSpeech(speechData: SpeechData): Response[IO] =
    indexSpeechAsPlainText(speechData.asJson.toString)

  private[this] def indexSpeechAsPlainText(speechJson: String): Response[IO] = {
    val postSpeech =
      Request[IO](Method.POST, uri"/indexes").withEntity(speechJson)
    this.server.run(postSpeech).unsafeRunSync
  }
}
