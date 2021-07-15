package htw.ai.p2p.speechsearch.api.peers

import htw.ai.p2p.speechsearch.domain.core.invertedindex.InvertedIndex.PostingList
import io.circe.Json

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
sealed trait PeerResponse

case class SuccessData(
  error: Boolean = false,
  key: String,
  value: Json = Json.Null
) extends PeerResponse

case class PostingsData(
  error: Boolean = false,
  key: String,
  value: PostingList = Nil
) extends PeerResponse

case class ErrorData(
  error: Boolean,
  errorMsg: String
) extends PeerResponse

sealed trait PeerRequest

case class InsertionData(data: PostingList) extends PeerRequest
