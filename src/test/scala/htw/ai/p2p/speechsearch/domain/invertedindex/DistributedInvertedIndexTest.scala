package htw.ai.p2p.speechsearch.domain.invertedindex

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestData._
import htw.ai.p2p.speechsearch.domain.model.speech.{DocId, Posting}

class DistributedInvertedIndexTest extends BaseShouldSpec {
  val dii = DistributedInvertedIndex.apply(new DHTClientTest())

  "a distributed index" should "get single posting list" in {
    val getResult = dii.get("test")
    getResult should contain allElementsOf List(Posting(DocId(ValidUuid1), 1, 100), Posting(DocId(ValidUuid2), 2, 10))
  }

  "a distributed index" should "get multiple posting list" in {
    val getResult = dii.getAll(List[String]("linux", "windows"))
    getResult should contain allElementsOf Map(("linux", List(Posting(DocId(ValidUuid1), 1, 100), Posting(DocId(ValidUuid2), 2, 10))), ("windows", List(Posting(DocId(ValidUuid1), 1, 100), Posting(DocId(ValidUuid2), 2, 10))))
  }

  "a distributed index" should "be able tp get multiple posting list even if some keys have no results" in {
    val getResult = dii.getAll(List[String]("testHasResults", "testNoResults"))
    getResult should contain allElementsOf Map(("testHasResults", List(Posting(DocId(ValidUuid1), 1, 100), Posting(DocId(ValidUuid2), 2, 10))), ("testNoResults", List()))
  }
}
