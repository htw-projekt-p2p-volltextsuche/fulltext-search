package htw.ai.p2p.speechsearch.api.errors

import htw.ai.p2p.speechsearch.config.CirceConfig._
import htw.ai.p2p.speechsearch.domain.model.search.Search
import htw.ai.p2p.speechsearch.domain.model.speech.Speech

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
sealed trait ApiError extends Exception

case class SearchError(search: Search)       extends ApiError
case class IndexError(speeches: Seq[Speech]) extends ApiError

sealed trait PeerError                             extends ApiError
case class PeerServerFailure(message: String)      extends PeerError
case class PeerServerError(cause: Throwable)       extends PeerError
case class PeerServiceUnavailable(message: String) extends PeerError
case class PeerConnectionError(cause: Throwable)   extends PeerError
