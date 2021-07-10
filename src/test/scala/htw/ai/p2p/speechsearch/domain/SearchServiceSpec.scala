package htw.ai.p2p.speechsearch.domain

import cats.effect.IO
import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.SpeechSearchServer.unsafeLogger
import htw.ai.p2p.speechsearch.TestData._
import htw.ai.p2p.speechsearch.TestUtils.{TestSearchResult, readSpeechFromFile}
import htw.ai.p2p.speechsearch.api.searches.SearchService
import htw.ai.p2p.speechsearch.domain.model.search.Connector.AndNot
import htw.ai.p2p.speechsearch.domain.model.search.FilterCriteria.Affiliation
import htw.ai.p2p.speechsearch.domain.model.search._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class SearchServiceSpec extends BaseShouldSpec {

  private val speech1 = readSpeechFromFile(
    "speech_carsten_schneider_23_04_2021.json"
  )
  private val speech2 = readSpeechFromFile("speech_olaf_scholz_23_04_2021.json")

  private val searchService = for {
    ii      <- seededIndex(speech1, speech2)
    searcher = Searcher(TestTokenizer)
  } yield SearchService.impl[IO](searcher, ii)

  "A Searcher" should "intersect results of single coherent query" in {
    val search = Search(Query("Mittwoch Bundesnotbremse"))

    searchService.flatMap(_.create(search)).asserting {
      _.docIds should contain only speech2.docId
    }
  }

  it should "ignore stop words in a query" in {
    val search = Search(query = Query(terms = "Er soll seine Arbeit machen"))

    searchService.flatMap(_.create(search)).asserting {
      _.docIds should contain only speech1.docId
    }
  }

  it should "unite the results of an OR query" in {
    val search = Search(
      Query(
        terms = "Mittwoch",
        additions = List(
          QueryElement(connector = Connector.Or, terms = "Bundesnotbremse")
        )
      )
    )

    searchService.flatMap(_.create(search)).asserting {
      _.docIds should contain theSameElementsAs List(
        speech1.docId,
        speech2.docId
      )
    }
  }

  it should "sort out corresponding results of an AND_NOT query" in {
    val search = Search(
      Query("Mittwoch", List(QueryElement(AndNot, "Bundesnotbremse")))
    )

    searchService.flatMap(_.create(search)).asserting {
      _.docIds should contain only speech1.docId
    }
  }

  it should "apply filter properly to a query" in {
    val search = Search(
      query = Query("Mittwoch"),
      filter = List(
        QueryFilter(
          criteria = Affiliation,
          value = "spd"
        )
      )
    )

    searchService.flatMap(_.create(search)).asserting { result =>
      result.docIds should contain only speech1.docId
      result.results.head.score should be > 0.0
    }
  }

  it should "return the correct total count when there are more results than max_results" in {
    val search = Search(
      query = Query("Mittwoch"),
      maxResults = 1
    )

    searchService.flatMap(_.create(search)).asserting {
      _.total shouldBe 2
    }
  }

  it should "ignore blank filter values" in {
    val search = Search(
      query = Query("Mittwoch"),
      filter = List(
        QueryFilter(criteria = FilterCriteria.Affiliation, value = ""),
        QueryFilter(criteria = FilterCriteria.Speaker, value = "    ")
      )
    )

    searchService.flatMap(_.create(search)).asserting {
      _.total should be > 0
    }
  }

  it should "combine filters of same type with OR among themselves" in {
    val search = Search(
      query = Query("Mittwoch"),
      filter = List(
        QueryFilter(criteria = FilterCriteria.Affiliation, value = "spd"),
        QueryFilter(criteria = FilterCriteria.Affiliation, value = "fdp")
      )
    )

    searchService.flatMap(_.create(search)).asserting {
      _.total shouldBe 2
    }
  }

}