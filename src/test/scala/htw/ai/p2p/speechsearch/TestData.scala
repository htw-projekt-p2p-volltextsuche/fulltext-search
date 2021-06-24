package htw.ai.p2p.speechsearch

import htw.ai.p2p.speechsearch.domain.model.search.Connector.And
import htw.ai.p2p.speechsearch.domain.model.search.FilterCriteria.Speaker
import htw.ai.p2p.speechsearch.domain.model.search._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object TestData {

  val entireSearch: Search = Search(
    maxResults = 15,
    query = Query(
      terms = "hello",
      additions = List(QueryElement(And, "world"))
    ),
    filter = List(
      QueryFilter(criteria = Speaker, value = "Nelson Mandela")
    )
  )
}
