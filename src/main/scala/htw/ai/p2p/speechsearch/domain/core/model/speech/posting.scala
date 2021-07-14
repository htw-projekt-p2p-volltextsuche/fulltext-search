package htw.ai.p2p.speechsearch.domain.core.model.speech

import htw.ai.p2p.speechsearch.config.CirceConfig._
import io.circe.Codec
import io.circe.generic.extras.semiauto._

import java.util.UUID

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
case class Posting(docId: DocId, tf: Int, docLen: Int)

case class DocId(self: UUID) extends AnyVal

object DocId {

  implicit val docIdCodec: Codec[DocId] = deriveUnwrappedCodec

  def apply(value: UUID): DocId = new DocId(value)

}

object Posting {

  implicit val postingCodec: Codec[Posting] = deriveConfiguredCodec

}
