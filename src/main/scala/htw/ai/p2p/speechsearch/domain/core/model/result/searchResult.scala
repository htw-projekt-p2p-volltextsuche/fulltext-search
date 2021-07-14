package htw.ai.p2p.speechsearch.domain.core.model.result

import htw.ai.p2p.speechsearch.config.CirceConfig._
import htw.ai.p2p.speechsearch.domain.core.model.speech.DocId
import SearchResult._
import htw.ai.p2p.speechsearch.api.PageInfo
import io.circe.Codec
import io.circe.generic.extras.semiauto._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
case class SearchResult(total: Int, results: List[ResultEntry]) {

  def paginate(pageInfo: PageInfo): SearchResult =
    SearchResult(
      total,
      results.slice(pageInfo.offset, pageInfo.offset + pageInfo.limit)
    )

}

case class ResultEntry(docId: DocId, score: Score)

object SearchResult {

  type Score = Double

  implicit val searchResultCodec: Codec[SearchResult] = deriveConfiguredCodec
  implicit val resultEntryCodec: Codec[ResultEntry]   = deriveConfiguredCodec

}
