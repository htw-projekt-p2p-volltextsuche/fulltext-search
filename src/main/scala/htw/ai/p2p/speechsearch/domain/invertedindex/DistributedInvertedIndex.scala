package htw.ai.p2p.speechsearch.domain.invertedindex

import htw.ai.p2p.speechsearch.ApplicationConfig._
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex.Term
import htw.ai.p2p.speechsearch.domain.model.speech.Posting
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.parser.decode
import io.circe.{Codec, Json}

object DistributedInvertedIndex {
  def apply(client: DHTClient): InvertedIndex = new DistributedInvertedIndex(client)
}

class DistributedInvertedIndex(client: DHTClient)
extends InvertedIndex {

  override def size: Int = ??? //client.get("size")

  override def insert(term: Term, posting: Posting): InvertedIndex = {
    client.post(term, posting)
    this
  }

  override def insertAll(entries: Map[Term, Posting]): InvertedIndex = {
    client.postMany(entries)
    this
  }

  override def get(term:  Term): List[Posting] = {
    decode[ResponseDTO](client.get(term)).fold(
      _ => Nil,
      _.toDomain()
    )
  }

  override def getAll(terms:  List[Term]): Map[Term, List[Posting]] = {
    decode[ResponseMapDTO](client.getMany(terms)).fold(
      _ => Map.empty,
      _.toDomain()
    )
  }
}

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

object ResponseDTO {
  implicit val responseCodec: Codec[ResponseDTO] = deriveConfiguredCodec
}

object ResponseMapDTO {
  implicit val responseMapCodec: Codec[ResponseMapDTO] = deriveConfiguredCodec
}

object PostingListWithoutKey {
  implicit val responsePostingListWithoutKeyCodec: Codec[PostingListWithoutKey] = deriveConfiguredCodec
}