package htw.ai.p2p.speechsearch.domain.model

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
case class SearchQuery(
    maxResults: Int = 25,
    target: QueryTarget = FullText,
    terms: String,
    extensions: List[QueryExtension]
)

case class QueryExtension(
    connector: QueryConnector = Or,
    target: QueryTarget = FullText,
    terms: String
)

sealed trait QueryTarget
case object FullText extends QueryTarget
case object Affiliation extends QueryTarget
case object Speaker extends QueryTarget

sealed trait QueryConnector
case object Or extends QueryConnector
case object And extends QueryConnector
case object AndNot extends QueryConnector
