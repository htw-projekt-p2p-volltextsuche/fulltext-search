package htw.ai.p2p.speechsearch.domain.model

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
case class SearchQuery(
    maxResults: Int = 25,
    searchType: SearchType = FullText,
    terms: String,
    extensions: List[QueryExtension] = Nil
)

case class QueryExtension(
    connector: QueryConnector = Or,
    searchType: SearchType = FullText,
    terms: String
)

sealed trait SearchType
case object FullText extends SearchType
case object Affiliation extends SearchType
case object Speaker extends SearchType

sealed trait QueryConnector
case object Or extends QueryConnector
case object And extends QueryConnector
case object AndNot extends QueryConnector
