package htw.ai.p2p.speechsearch.domain.core.model.search

import htw.ai.p2p.speechsearch.config.CirceConfig._
import io.circe._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._

case class Search(
  query: Query,
  filter: List[QueryFilter] = Nil,
  searchId: Option[String] = None
)

object Search {

  val SearchJsonKey = "search"

  implicit val codec: Codec[Search] = deriveConfiguredCodec

}
