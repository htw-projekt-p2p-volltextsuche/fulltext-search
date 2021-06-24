package htw.ai.p2p.speechsearch.domain.model.speech

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
case class Posting(docId: DocId, tf: Int, docLen: Int)

case class DocId(value: String) extends AnyVal

object DocId {

  implicit val docIdCodec: Codec[DocId] = deriveUnwrappedCodec

}
