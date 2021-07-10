package htw.ai.p2p.speechsearch.domain.invertedindex.old

import htw.ai.p2p.speechsearch.domain.model.speech.Posting
import org.http4s._

sealed trait Resp
case class PostingList(body: String) extends Resp

trait DHTClient {
  def get(key: String): String

  def getMany(terms: List[String]): String

  def post(key: String, data: Posting): Unit

  def postMany(entries: Map[String, Posting]): Unit
}

object DHTClient {
  def apply(uri: Uri): DHTClient =
    new DHTClientProduction(uri)
}
