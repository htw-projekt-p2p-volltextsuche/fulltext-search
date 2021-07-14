package htw.ai.p2p.speechsearch.api.searches

import cats.effect.IO
import cats.implicits._
import com.olegpy.meow.hierarchy._
import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.SpeechSearchServer.unsafeLogger
import htw.ai.p2p.speechsearch.TestData._
import htw.ai.p2p.speechsearch.TestUtils.{TestSearch, readFile, readSpeechesFromFile}
import htw.ai.p2p.speechsearch.api.PageInfo
import htw.ai.p2p.speechsearch.api.errors._
import htw.ai.p2p.speechsearch.api.index.{IndexRoutes, IndexService}
import htw.ai.p2p.speechsearch.domain.core.model.result.SearchResult
import htw.ai.p2p.speechsearch.domain.core.model.search.{Query, Search}
import htw.ai.p2p.speechsearch.domain.core.model.speech.Speech
import htw.ai.p2p.speechsearch.domain.core.{Indexer, Searcher}
import htw.ai.p2p.speechsearch.domain.lru.LruRef
import io.chrisdavenport.log4cats.Logger
import io.circe._
import io.circe.literal.JsonStringContext
import io.circe.parser._
import io.circe.syntax.EncoderOps
import org.http4s.Method.POST
import org.http4s.Status.Ok
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class SearchRoutesSpec extends BaseShouldSpec {

  implicit val eh: HttpErrorHandler[IO, ApiError] = new ApiErrorHandler[IO]

  val server: IO[HttpApp[IO]] = {
    for {
      ii             <- TestInvertedIndex
      searchCacheRef <- LruRef.empty[IO, Search, SearchResult](0)
      searcher        = Searcher(TestTokenizer)
      searchService   = SearchService.impl[IO](searcher, ii, searchCacheRef)
      indexer         = Indexer(TestTokenizer)
      indexService    = IndexService.impl[IO](indexer, ii)
    } yield (
      SearchRoutes.routes(searchService) <+> IndexRoutes.routes(indexService)
    ).orNotFound
  }

  "The Route /searches" should "return status code 200 for valid search" in {
    val response = postSearch(EntireSearch.paginated.asJson)

    response.asserting(_.status shouldBe Ok)
  }

  it should "return valid SearchResult" in {
    val searchResult =
      for {
        response <- postSearch(EntireSearch.paginated.asJson)
        result   <- response.as[SearchResult]
      } yield result

    searchResult.asserting { result =>
      result.results shouldBe empty
      result.total shouldBe 0
    }
  }

  it should "decode speech from json properly" in {
    val search = readFile("valid_paginated_search.json")

    val response = postSearch(search)

    response.asserting(_.status shouldBe Ok)
  }

  it should "throw InvalidMessageBodyFailure when invalid body is sent" in {
    val invalidSearch = json"""{"search":{"max_results":42}}"""

    postSearch(invalidSearch).assertThrows[InvalidMessageBodyFailure]
  }

  it should "paginate the results according to the request" in {
    val firstSearch = PaginatedSearch(
      search = Search(Query("Bundestag")),
      pageInfo = PageInfo(0, 20)
    )
    val secondSearch = PaginatedSearch(
      search = Search(Query("Bundestag")),
      pageInfo = PageInfo(10, 10)
    )
    val results = for {
      s            <- server
      _            <- indexSpeeches(s)
      firstResult  <- postSearch(firstSearch, s)
      secondResult <- postSearch(secondSearch, s)
    } yield (firstResult, secondResult)

    results.asserting { case (first, second) =>
      first.results should have size 20
      second.results should have size 10
      second.results should contain allElementsOf first.results.slice(10, 20)
    }
  }

  private def postSearch(search: PaginatedSearch, s: HttpApp[IO]): IO[SearchResult] =
    s.run(
      Request[IO](POST, uri"/searches").withEntity(search.asJson)
    ) >>= (_.as[SearchResult])

  private def indexSpeeches(s: HttpApp[IO]): IO[Response[IO]] =
    s.run(
      Request[IO](POST, uri"/index/speeches")
        .withEntity(readSpeechesFromFile("sample_speeches.json").asJson)
    )

  private def postSearch(json: String): IO[Response[IO]] =
    postSearch(parse(json).fold(throw _, identity))

  private def postSearch[Logger](json: Json): IO[Response[IO]] =
    Logger[IO].info(
      s"Sending POST request to /searches with body: ${System.lineSeparator()}$json"
    ) *> server >>= (
      _.run(Request[IO](POST, uri"/searches").withEntity(json))
    )

  private def indexSpeeches(speeches: List[Speech]): IO[Response[IO]] = {
    val postSpeech =
      Request[IO](Method.POST, uri"/index/speeches").withEntity(speeches.asJson)
    server.flatMap(_.run(postSpeech))
  }

}
