package htw.ai.p2p.speechsearch.api

import cats.effect.IO
import cats.effect.concurrent.Ref
import htw.ai.p2p.speechsearch.api.routes.SearchRoutes
import htw.ai.p2p.speechsearch.api.service.Searches
import htw.ai.p2p.speechsearch.api.service.Searches.Search
import htw.ai.p2p.speechsearch.domain.{Index, LocalInvertedIndex, Tokenizer}
import htw.ai.p2p.speechsearch.domain.model.SearchResult
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe.jsonEncoder
import org.http4s.implicits._

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
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
    assertIO(searchResult.map(_.results), Nil) // TODO
  }

  val server: HttpApp[IO] = {
    val index = Index(Tokenizer(), LocalInvertedIndex())
    val indexRef = Ref[IO].of(index).unsafeRunSync
    val searches = Searches.impl[IO](indexRef)
    new SearchRoutes(searches).routes.orNotFound
  }

  private[this] def searchQuery(search: Search): Response[IO] = {
    val postSearch =
      Request[IO](Method.POST, uri"/searches").withEntity(search.asJson)
    this.server.run(postSearch).unsafeRunSync()
  }
}
