package htw.ai.p2p.speechsearch.domain.invertedindex.responseDataTypes

import htw.ai.p2p.speechsearch.ApplicationConfig._
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex.Term
import htw.ai.p2p.speechsearch.domain.model.speech.Posting
import io.circe.{Codec, Json}
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.parser.decode


case class ResponseDTO(
                        error: Boolean,
                        key: String,
                        value: List[Posting]) {
  def toDomain(): List[Posting] = value
}

case class ResponseMapDTO(error: Boolean,
                          keys: List[String],
                          values: Json ) {
  def toDomain(): Map[Term, List[Posting]] = keys.map(key => {
    val json = values.findAllByKey(key)(0)
    if(json.findAllByKey("value") != null)
      (key, decode[PostingListWithoutKey](json.toString()).fold(_ => Nil, _.toDomain()))
    else (key, Nil)
  }).toMap
}

case class PostingListWithoutKey(error: Boolean,
                                 value: List[Posting]) {
  def toDomain(): List[Posting] = value
}

case class PutResponse(error: Boolean, key: String)

object ResponseDTO {
  implicit val responseCodec: Codec[ResponseDTO] = deriveConfiguredCodec
}

object ResponseMapDTO {
  implicit val responseMapCodec: Codec[ResponseMapDTO] = deriveConfiguredCodec
}

object PostingListWithoutKey {
  implicit val responsePostingListWithoutKeyCodec: Codec[PostingListWithoutKey] = deriveConfiguredCodec
}

object PutResponse {
  implicit val putResponse : Codec[PutResponse] = deriveConfiguredCodec
}