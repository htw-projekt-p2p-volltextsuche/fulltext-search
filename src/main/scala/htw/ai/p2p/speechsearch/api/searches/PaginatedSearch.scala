package htw.ai.p2p.speechsearch.api.searches

import htw.ai.p2p.speechsearch.config.CirceConfig._
import htw.ai.p2p.speechsearch.api.PageInfo
import htw.ai.p2p.speechsearch.domain.core.model.search.Search
import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
case class PaginatedSearch(
  search: Search,
  pageInfo: PageInfo = PageInfo()
)

object PaginatedSearch {

  implicit val paginatedSearchCodec: Codec[PaginatedSearch] = deriveConfiguredCodec

}
