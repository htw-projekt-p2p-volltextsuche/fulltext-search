package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestUtils.readSpeechFromFile
import htw.ai.p2p.speechsearch.domain.SearcherSpec.TestSearchResult
import htw.ai.p2p.speechsearch.domain.model.result.SearchResult
import htw.ai.p2p.speechsearch.domain.model.search.Connector.AndNot
import htw.ai.p2p.speechsearch.domain.model.search.FilterCriteria.Affiliation
import htw.ai.p2p.speechsearch.domain.model.search._
import htw.ai.p2p.speechsearch.domain.model.speech.DocId

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class SearcherSpec extends BaseShouldSpec {

  private val speech1 = readSpeechFromFile(
    "speech_carsten_schneider_23_04_2021.json"
  )
  private val speech2 = readSpeechFromFile("speech_olaf_scholz_23_04_2021.json")
  private val nextIndex = Index().index(speech1).index(speech2)
  private val searcher = Searcher(nextIndex)

  "A Searcher" should "intersect results of single coherent query" in {
    val search = Search(Query("Mittwoch Bundesnotbremse"))

    val result = searcher.search(search)

    result.docIds should contain only speech2.docId
  }

  it should "ignore stop words in a query" in {
    val search = Search(query = Query(terms = "Er soll seine Arbeit machen"))

    val result = searcher.search(search)

    result.docIds should contain only speech1.docId
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

    val result = searcher.search(search)

    result.docIds should contain theSameElementsAs List(speech1.docId, speech2.docId)
  }

  it should "sort out corresponding results of an AND_NOT query" in {
    val search = Search(
      Query("Mittwoch", List(QueryElement(AndNot, "Bundesnotbremse")))
    )

    val result = searcher.search(search)

    result.docIds should contain only speech1.docId
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

    val result = searcher.search(search)

    result.docIds should contain only speech1.docId
    result.results.head.score should be > 0.0
  }

  it should "return the correct total count when there are more results than max_results" in {
    val search = Search(
      query = Query("Mittwoch"),
      maxResults = 1
    )

    val results = searcher.search(search)

    results.total shouldBe 2
  }

}

object SearcherSpec {

  implicit class TestSearchResult(result: SearchResult) {

    def docIds: Seq[DocId] = result.results map (_.docId)

  }

}
