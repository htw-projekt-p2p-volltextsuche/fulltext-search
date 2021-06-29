package htw.ai.p2p.speechsearch.domain.invertedindex

import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex.Term
import htw.ai.p2p.speechsearch.domain.model.speech.Posting
import io.circe.HCursor
import io.circe.parser.parse


object DistributedInvertedIndex {
  def apply(): InvertedIndex = new DistributedInvertedIndex()
}


class DistributedInvertedIndex private (client: DHTClient = new DHTClient())
extends InvertedIndex {
  override def size: Int = ???

  override def insert(term: Term, posting: Posting): InvertedIndex = {
    client.post(term, posting)
    this
  }

  override def insertAll(entries: Map[Term, Posting]): InvertedIndex = {
    client.postMany(entries)
    this
  }

  override def get(term:  Term): List[Posting] = {
    parse(client.get(term)) match {
      case Left(failure) => null
      case Right(json) => {
        val cursor: HCursor = json.hcursor
        cursor.downField("value").as[List[Posting]] match {
          case Left(failure) => null
          case Right(result) => result
        }
      }
    }
  }

  override def getAll(terms:  List[Term]): Map[Term, List[Posting]] = {
    terms.map(term => (term, get(term))).toMap

    //client.getMany(terms).asJson.as[Map[Term, PostingList]].orElse(null)
  }
}
