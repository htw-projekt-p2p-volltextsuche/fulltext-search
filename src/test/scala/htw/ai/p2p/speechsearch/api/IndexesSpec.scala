package htw.ai.p2p.speechsearch.api

import cats.effect.IO
import cats.effect.concurrent.Ref
import htw.ai.p2p.speechsearch.TestUtils.readSpeechFromFile
import htw.ai.p2p.speechsearch.api.routes.IndexRoutes
import htw.ai.p2p.speechsearch.api.service.Indexes
import htw.ai.p2p.speechsearch.domain.model.Speech
import htw.ai.p2p.speechsearch.domain.{Index, LocalInvertedIndex, Tokenizer}
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.jsonEncoder
import org.http4s.implicits._

/**
  * @author Joscha Seelig <jduesentrieb> 2021
 **/
class IndexesSpec extends CatsEffectSuite {

  test("should return status code 200 after successful indexing") {
    val speech = readSpeechFromFile("speech_carsten_schneider_23_04_2021.txt")

    val response: Response[IO] = indexSpeech(speech)

    assert(
      response.status == Status.Ok,
      s"Expected '${response.status}' to be '${Status.Ok}'"
    )
  }

  private val server: HttpApp[IO] = {
    val index = Index(Tokenizer(), LocalInvertedIndex())
    val indexRef = Ref[IO].of(index).unsafeRunSync
    val indexes = Indexes.impl(indexRef)
    new IndexRoutes(indexes).routes.orNotFound
  }

  private[this] def indexSpeech(speech: Speech): Response[IO] = {
    val postSpeech =
      Request[IO](Method.POST, uri"/indexes").withEntity(speech.asJson)
    this.server.run(postSpeech).unsafeRunSync
  }
}
