package htw.ai.p2p.speechsearch.api

import htw.ai.p2p.speechsearch.api.indexes.SpeechData
import htw.ai.p2p.speechsearch.api.searches.QueryData

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
sealed trait ApiError

final case class SearchError(queryData: QueryData, message: String)
    extends ApiError

final case class IndexError(speechData: SpeechData, message: String)
    extends ApiError
