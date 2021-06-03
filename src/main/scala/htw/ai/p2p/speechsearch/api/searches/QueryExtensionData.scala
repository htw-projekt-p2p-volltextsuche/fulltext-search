package htw.ai.p2p.speechsearch.api.searches

import htw.ai.p2p.speechsearch.domain.model.{Affiliation, Or, QueryExtension}

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
final case class QueryExtensionData(
    connector: String,
    `type`: String,
    terms: String
) {
  def toDomain: QueryExtension =
    QueryExtension(
      connector = Or, // todo
      searchType = Affiliation, // todo
      terms = terms
    )
}
