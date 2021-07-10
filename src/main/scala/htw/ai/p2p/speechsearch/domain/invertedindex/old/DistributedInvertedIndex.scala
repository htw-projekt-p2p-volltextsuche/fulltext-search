package htw.ai.p2p.speechsearch.domain.invertedindex.old

import htw.ai.p2p.speechsearch.config.CirceConfig._
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex.Term
import htw.ai.p2p.speechsearch.domain.model.speech.Posting
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.parser.decode
import io.circe.{Codec, Json}

object DistributedInvertedIndex {
  def apply(client: DHTClient): DistributedInvertedIndex =
    new DistributedInvertedIndex(client)
}

class DistributedInvertedIndex(client: DHTClient) {

  private val CorpusSizeKey = "_keyset_size"

  def size: Int = client.get(CorpusSizeKey).toIntOption.getOrElse(1)

  def insert(term: Term, posting: Posting): DistributedInvertedIndex = {
    client.post(term, posting)
    this
  }

  def insertAll(entries: Map[Term, Posting]): DistributedInvertedIndex = {
    client.postMany(entries)
    this
  }

  def get(term: Term): List[Posting] =
    decode[ResponseDTO](client.get(term)).fold(
      _ => Nil,
      _.toDomain
    )

  def getAll(terms: List[Term]): Map[Term, List[Posting]] =
    decode[ResponseMapDTO](client.getMany(terms)).fold(
      _ => Map.empty,
      _.toDomain
    )
}

case class ResponseDTO(error: Boolean, key: String, value: List[Posting]) {
  def toDomain: List[Posting] = value
}

case class ResponseMapDTO(error: Boolean, keys: List[String], values: Json) {
  def toDomain: Map[Term, List[Posting]] = keys.map { key =>
    val json = values.findAllByKey(key).head
    if (json.findAllByKey("value") != null)
      (
        key,
        decode[PostingListWithoutKey](json.toString()).fold(_ => Nil, _.toDomain)
      )
    else (key, Nil)
  }.toMap
}

case class PostingListWithoutKey(error: Boolean, value: List[Posting]) {
  def toDomain: List[Posting] = value
}

object ResponseDTO {
  implicit val responseCodec: Codec[ResponseDTO] = deriveConfiguredCodec
}

object ResponseMapDTO {
  implicit val responseMapCodec: Codec[ResponseMapDTO] = deriveConfiguredCodec
}

object PostingListWithoutKey {
  implicit val responsePostingListWithoutKeyCodec: Codec[PostingListWithoutKey] =
    deriveConfiguredCodec
}