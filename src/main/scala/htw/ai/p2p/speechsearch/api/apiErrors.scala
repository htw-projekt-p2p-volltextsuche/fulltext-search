package htw.ai.p2p.speechsearch.api

import htw.ai.p2p.speechsearch.CirceConfig._
import htw.ai.p2p.speechsearch.domain.model.search.Search
import htw.ai.p2p.speechsearch.domain.model.speech.Speech
import io.circe.Codec
import io.circe.generic.extras.semiauto._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
sealed trait ApiError

object ApiError {

  implicit val codec: Codec[ApiError] = deriveConfiguredCodec

}

final case class SearchError(search: Search, message: String) extends ApiError

final case class IndexError(speech: Seq[Speech], message: String) extends ApiError
