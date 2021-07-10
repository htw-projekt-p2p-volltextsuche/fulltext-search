package htw.ai.p2p.speechsearch.api.errors

import htw.ai.p2p.speechsearch.config.CirceConfig._
import htw.ai.p2p.speechsearch.domain.model.search.Search
import htw.ai.p2p.speechsearch.domain.model.speech.Speech
import org.http4s.client.ConnectionFailure

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
sealed trait ApiError extends Exception

case class SearchError(search: Search)                   extends ApiError
case class IndexError(speeches: Seq[Speech])             extends ApiError
case class PeerServerFailure(message: String)            extends ApiError
case class PeerServerError(cause: Throwable)             extends ApiError
case class PeerConnectionError(cause: ConnectionFailure) extends ApiError
