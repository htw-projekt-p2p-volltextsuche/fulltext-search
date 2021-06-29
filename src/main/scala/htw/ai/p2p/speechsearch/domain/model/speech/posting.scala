package htw.ai.p2p.speechsearch.domain.model.speech

import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.{Codec, Decoder, Encoder}

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
  implicit val postingEncoder: Encoder[Posting] =
    Encoder.forProduct3("docId", "tf", "docLen")(p => (p.docId, p.tf, p.docLen))
  implicit val postingDecoder: Decoder[Posting] =
    Decoder.forProduct3("docId", "tf", "docLen")(Posting.apply)
}
