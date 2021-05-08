package htw.ai.p2p.speechsearch

import cats.effect.IO
import htw.ai.p2p.speechsearch.api.Searches
import htw.ai.p2p.speechsearch.api.Searches.{Search, SearchResult}
import io.circe.syntax._
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.jsonEncoder
import org.http4s.implicits._

class SearchesSpec extends CatsEffectSuite {

  test("Search returns status code 200") {
    val searchResponse: Response[IO] = searchQuery(Search(query = "test"))
    assert(
      searchResponse.status == Status.Ok,
      s"Expected '${searchResponse.status}' to be '${Status.Ok}'"
    )
  }

  test("Search returns SearchResult") {
    val searchResult = searchQuery(Search(query = "test")).as[SearchResult]
    assertIO(searchResult.map(_.postings), Nil)
  }

  val server: HttpApp[IO] = {
    val searches = Searches.impl[IO]
    SpeechSearchRoutes.searchRoutes(searches).orNotFound
  }

  private[this] def searchQuery(search: Search): Response[IO] = {
    val postSearch =
      Request[IO](Method.POST, uri"/searches").withEntity(search.asJson)
    this.server.run(postSearch).unsafeRunSync()
  }
}
