package htw.ai.p2p.speechsearch.domain.model.search

import htw.ai.p2p.speechsearch.ApplicationConfig._
import htw.ai.p2p.speechsearch.domain.model.search.Search.MaxResultsDefault
import io.circe._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._

case class Search(
  query: Query,
  filter: List[QueryFilter] = Nil,
  maxResults: Int = MaxResultsDefault
)

object Search {

  val MaxResultsDefault = 25
  val SearchJsonKey     = "search"

  implicit val decoder: Decoder[Search] =
    deriveConfiguredDecoder[Search] prepare (_.downField(SearchJsonKey))
  implicit val encoder: Encoder[Search] =
    deriveConfiguredEncoder[Search] mapJson (j => Json.obj((SearchJsonKey, j)))

}
