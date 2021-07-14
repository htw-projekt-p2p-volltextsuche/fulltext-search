package htw.ai.p2p.speechsearch.api

import htw.ai.p2p.speechsearch.config.CirceConfig._
import htw.ai.p2p.speechsearch.api.PageInfo.PageSizeDefault
import io.circe.Codec
import io.circe.generic.extras.semiauto._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
case class PageInfo(offset: Int = 0, limit: Int = PageSizeDefault)

object PageInfo {

  val PageSizeDefault = 25

  implicit val pageInfoCodec: Codec[PageInfo] = deriveConfiguredCodec

}
