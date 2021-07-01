package htw.ai.p2p.speechsearch.domain.model.speech

import htw.ai.p2p.speechsearch.CirceConfig._
import io.circe.Codec
import io.circe.generic.extras.semiauto._

import java.util.UUID
import scala.util.Try

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
case class Posting(docId: DocId, tf: Int, docLen: Int)

case class DocId(self: UUID) extends AnyVal

object DocId {

  implicit val docIdCodec: Codec[DocId] = deriveUnwrappedCodec

  def apply(value: UUID): DocId = new DocId(value)

  def apply(value: String): Option[DocId] =
    Try(DocId(UUID fromString value)).toOption

}

object Posting {

  implicit val postingCodec: Codec[Posting] = deriveConfiguredCodec

}
