package htw.ai.p2p.speechsearch.api.searches

import htw.ai.p2p.speechsearch.api.PageInfo
import htw.ai.p2p.speechsearch.domain.core.model.result.SearchResult

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
case class PaginatedSearchResult(
  result: SearchResult,
  pageInfo: PageInfo
)
