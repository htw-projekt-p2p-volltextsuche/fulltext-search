package htw.ai.p2p.speechsearch.domain.model.result

import htw.ai.p2p.speechsearch.config.CirceConfig._
import htw.ai.p2p.speechsearch.domain.model.result.SearchResult._
import htw.ai.p2p.speechsearch.domain.model.speech.DocId
import io.circe.Codec
import io.circe.generic.extras.semiauto._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
case class SearchResult(total: Int, results: List[ResultEntry])

case class ResultEntry(docId: DocId, score: Score)

object SearchResult {

  type Score = Double

  implicit val searchResultCodec: Codec[SearchResult] = deriveConfiguredCodec
  implicit val resultEntryCodec: Codec[ResultEntry]   = deriveConfiguredCodec

}
