package htw.ai.p2p.speechsearch.api.searches

import cats.effect.IO
import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.SpeechSearchServer.unsafeLogger
import htw.ai.p2p.speechsearch.TestData._
import htw.ai.p2p.speechsearch.TestUtils.readFile
import htw.ai.p2p.speechsearch.domain._
import htw.ai.p2p.speechsearch.domain.model.result.SearchResult
import io.circe._
import io.circe.literal.JsonStringContext
import io.circe.parser._
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.slf4j.{Logger, LoggerFactory}

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class SearchRoutesSpec extends BaseShouldSpec {

  private val Logger: Logger = LoggerFactory.getLogger(getClass)

  "The Route /searches" should "return status code 200 for valid search" in {
    val response = postSearch(EntireSearch.asJson)

    response.status shouldBe Status.Ok
  }

  it should "return valid SearchResult" in {
    val searchResult = postSearch(EntireSearch.asJson).as[SearchResult]

    searchResult.asserting { result =>
      result.results shouldBe empty
      result.total shouldBe 0
    }
  }

  it should "decode speech from json properly" in {
    val search = readFile("valid_search.json")

    val response = postSearch(search)

    response.status shouldBe Status.Ok
  }

  it should "return throw InvalidMessageBodyFailure when invalid body is sent" in {
    val invalidSearch = json"""{"search":{"max_results":42}}"""

    a[InvalidMessageBodyFailure] should be thrownBy postSearch(invalidSearch)
  }

  val server: IO[HttpApp[IO]] =
    for {
      ii      <- TestInvertedIndex
      searcher = Searcher(TestTokenizer)
      searches = SearchService.impl[IO](searcher, ii)
    } yield new SearchRoutes(searches).routes.orNotFound

  private[this] def postSearch(json: String): Response[IO] =
    postSearch(parse(json).fold(throw _, identity))

  private[this] def postSearch(json: Json): Response[IO] = {
    Logger.info(
      s"Sending POST request to /searches with body: ${System.lineSeparator()}",
      json
    )
    val postSearch = Request[IO](Method.POST, uri"/searches").withEntity(json)
    this.server.flatMap(_.run(postSearch)).unsafeRunSync()
  }

}