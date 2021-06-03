package htw.ai.p2p.speechsearch.api.searches

import htw.ai.p2p.speechsearch.domain.model.{FullText, SearchQuery}

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
final case class QueryData(
    max_results: Int,
    `type`: String,
    terms: String,
    extensions: List[QueryExtensionData] = Nil
) {
  def toDomain: SearchQuery =
    SearchQuery(
      maxResults = max_results,
      searchType = FullText, // todo
      terms = terms,
      extensions = extensions map (_.toDomain)
    )
}
