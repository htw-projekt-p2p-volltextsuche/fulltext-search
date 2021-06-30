package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestData.{ValidUuid1, ValidUuid2}
import htw.ai.p2p.speechsearch.domain.invertedindex._
import htw.ai.p2p.speechsearch.domain.model.speech._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class LocalInvertedIndexSpec extends BaseShouldSpec {

  "A local Inverted Index" should "insert posting when term is not yet known" in {
    val posting = Posting(DocId(ValidUuid1), 1, 100)

    val index = LocalInvertedIndex().insert("test", posting)

    val result = index.get("test")
    assert(result.contains(posting), s"$result does not contain $posting")
  }

  it should "also insert with infix alias" in {
    val posting = Posting(DocId(ValidUuid1), 1, 100)

    val index: InvertedIndex = LocalInvertedIndex() :+ ("test" -> posting)

    val result = index.get("test")
    assert(result.contains(posting), s"$result does not contain $posting")
  }

  it should "also get with apply alias" in {
    val posting              = Posting(DocId(ValidUuid1), 1, 100)
    val index: InvertedIndex = LocalInvertedIndex().insert("test", posting)

    val result = index("test")

    assert(result.contains(posting), s"$result does not contain $posting")
  }

  it should "append posting when term is already present" in {
    val knownPosting = Posting(DocId(ValidUuid1), 1, 100)
    val newPosting   = Posting(DocId(ValidUuid2), 3, 100)
    val index = LocalInvertedIndex()
      .insert("test", knownPosting)
      .insert("test", newPosting)

    val result = index.get("test")

    result should contain allElementsOf List(knownPosting, newPosting)
  }

  it should "insert all postings when term is not yet known" in {
    val mappedPostings = Map(
      "term 1" -> Posting(DocId(ValidUuid1), 3, 100),
      "term 2" -> Posting(DocId(ValidUuid2), 3, 100)
    )

    val index = LocalInvertedIndex().insertAll(mappedPostings)

    index("term 1") should contain only mappedPostings("term 1")
  }

  it should "append all postings when terms are already present" in {
    val knownPosting = Posting(DocId(ValidUuid1), 1, 100)
    val newPosting   = Posting(DocId(ValidUuid2), 3, 100)
    val newPostings = Map(
      "term 1" -> newPosting,
      "term 2" -> Posting(DocId(ValidUuid1), 3, 100)
    )
    val index = LocalInvertedIndex() :+ ("term 1" -> knownPosting)

    val newIndex = index.insertAll(newPostings)

    val result = newIndex("term 1")

    newIndex("term 1") should contain allElementsOf List(knownPosting, newPosting)
  }

  it should "allow infix operator for insert all" in {
    val postings = Map(
      "term 1" -> Posting(DocId(ValidUuid1), 3, 100),
      "term 2" -> Posting(DocId(ValidUuid2), 3, 100)
    )

    val index = LocalInvertedIndex() :++ postings

    index("term 1") should not be empty
    index("term 2") should not be empty
  }

  it should "return a map with all terms and their associated postings" in {
    val index = LocalInvertedIndex().insertAll(
      Map(
        "term 1" -> Posting(DocId(ValidUuid1), 3, 100),
        "term 2" -> Posting(DocId(ValidUuid2), 3, 100)
      )
    )

    val result = index.getAll(List("term 1", "term 2"))

    result("term 1") should not be empty
    result("term 2") should not be empty
  }

  it should "return empty list when trying to get unknown keys" in {
    val result = LocalInvertedIndex().getAll(List("unknown"))

    result("unknown") shouldBe empty
  }

  it should "still return all known terms if another unknown key is given" in {
    val index =
      LocalInvertedIndex() :+ "known" -> Posting(DocId(ValidUuid1), 3, 100)

    val result = index.getAll(List("known", "unknown"))

    result.size shouldBe 2
    result("known") should not be empty
  }
}
