package htw.ai.p2p.speechsearch.domain.invertedindex

import java.util.UUID
import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex.Term
import htw.ai.p2p.speechsearch.domain.model.speech.{DocId, Posting}
import org.http4s.implicits.http4sLiteralsSyntax

class DistributedInvertedIndexTest extends BaseShouldSpec {
  val ValidUuid1: UUID = UUID.fromString("b272829e-f15b-44fe-8c25-25e3eff45300")
  val ValidUuid2: UUID = UUID.fromString("32271a61-5c42-452f-8d59-7a2323c88bff")

  val dii = DistributedInvertedIndex.apply(new DHTClientTest())

  "a distributed index" should "get single posting list" in {
    val getResult = dii.get("test")
    getResult should contain allElementsOf List(
      Posting(DocId(ValidUuid1), 1, 100),
      Posting(DocId(ValidUuid2), 2, 10)
    )
  }

  "a distributed index" should "get multiple posting list" in {
    val getResult = dii.getAll(List[String]("linux", "windows"))
    getResult should contain allElementsOf Map(
      (
        "linux",
        List(Posting(DocId(ValidUuid1), 1, 100), Posting(DocId(ValidUuid2), 2, 10))
      ),
      (
        "windows",
        List(Posting(DocId(ValidUuid1), 1, 100), Posting(DocId(ValidUuid2), 2, 10))
      )
    )
  }

  "a distributed index" should "be able to get multiple posting list even if some keys have no results" in {
    val getResult = dii.getAll(List[String]("testHasResults", "testNoResults"))
    getResult should contain allElementsOf Map(
      (
        "testHasResults",
        List(Posting(DocId(ValidUuid1), 1, 100), Posting(DocId(ValidUuid2), 2, 10))
      ),
      ("testNoResults", List())
    )
  }

  // These test require the p2p network to be running
  val diiProd = DistributedInvertedIndex.apply(
    new DHTClientProduction(uri"http://localhost:8090/")
  )

  behavior of "a distributed index with production client"

  ignore should "put new data" in {
    diiProd.insert("insertTerm", Posting(DocId(ValidUuid2), 2, 10))
    val getResult = diiProd.get("insertTerm")
    getResult should contain(Posting(DocId(ValidUuid2), 2, 10))
  }

  ignore should "put multiple new data" in {
    diiProd.insertAll(
      Map(
        "insertMultipleTerm1" -> Posting(DocId(ValidUuid2), 2, 10),
        "insertMultipleTerm2" -> Posting(DocId(ValidUuid1), 1, 10)
      )
    )
    val getResult: Map[Term, InvertedIndex.PostingList] =
      diiProd.getAll(List("insertMultipleTerm1", "insertMultipleTerm2"))
    getResult("insertMultipleTerm1") should contain(
      Posting(DocId(ValidUuid2), 2, 10)
    )
    getResult("insertMultipleTerm2") should contain(
      Posting(DocId(ValidUuid1), 1, 10)
    )
  }

  ignore should "get single posting list" in {
    val getResult = diiProd.get("notFindable")
    getResult should contain allElementsOf List()
  }

  ignore should "get single posting list 2" in {
    val getResult = diiProd.get("testDoc")
    getResult should contain allElementsOf List(Posting(DocId(ValidUuid1), 1, 100))
  }

  ignore should "get posting list for multiple keys" in {
    val getResult = diiProd.getAll(List("testDoc", "empty"))
    getResult should contain allElementsOf Map(
      "testDoc" -> List(Posting(DocId(ValidUuid1), 1, 100)),
      "empty"   -> Nil
    )
  }

}
