package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.domain.invertedindex._
import htw.ai.p2p.speechsearch.domain.model.speech._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class LocalInvertedIndexSpec extends BaseShouldSpec {

  "A local Inverted Index" should "insert posting when term is not yet known" in {
    val posting = Posting(DocId("123"), 1, 100)

    val index = LocalInvertedIndex().insert("test", posting)

    val result = index.get("test")
    assert(result.contains(posting), s"$result does not contain $posting")
  }

  it should "also insert with infix alias" in {
    val posting = Posting(DocId("123"), 1, 100)

    val index: InvertedIndex = LocalInvertedIndex() :+ ("test" -> posting)

    val result = index.get("test")
    assert(result.contains(posting), s"$result does not contain $posting")
  }

  it should "also get with apply alias" in {
    val posting              = Posting(DocId("123"), 1, 100)
    val index: InvertedIndex = LocalInvertedIndex().insert("test", posting)

    val result = index("test")

    assert(result.contains(posting), s"$result does not contain $posting")
  }

  it should "append posting when term is already present" in {
    val knownPosting = Posting(DocId("known"), 1, 100)
    val newPosting   = Posting(DocId("unknown"), 3, 100)
    val index        = LocalInvertedIndex().insert("test", knownPosting)

    val newIndex = index.insert("test", newPosting)

    val result = newIndex.get("test")
    assert(
      result.contains(newPosting),
      s"$result does not contain $newPosting"
    )
    assert(
      result.contains(knownPosting),
      s"$result does not contain $knownPosting"
    )
  }

  it should "insert all postings when term is not yet known" in {
    val mappedPostings = Map(
      "term 1" -> Posting(DocId("first"), 3, 100),
      "term 2" -> Posting(DocId("second"), 3, 100)
    )

    val index = LocalInvertedIndex().insertAll(mappedPostings)

    assert(index("term 1").contains(mappedPostings("term 1")))
    assert(index("term 2").contains(mappedPostings("term 2")))
  }

  it should "append all postings when terms are already present" in {
    val knownPosting = Posting(DocId("known"), 1, 100)
    val newPosting   = Posting(DocId("first"), 3, 100)
    val newPostings = Map(
      "term 1" -> newPosting,
      "term 2" -> Posting(DocId("second"), 3, 100)
    )
    val index = LocalInvertedIndex() :+ ("term 1" -> knownPosting)

    val newIndex = index.insertAll(newPostings)

    val result = newIndex("term 1")
    assert(
      result.contains(newPosting),
      s"$result does not contain $newPosting"
    )
    assert(
      result.contains(knownPosting),
      s"$result does not contain $knownPosting"
    )
  }

  it should "allow infix operator for insert all" in {
    val postings = Map(
      "term 1" -> Posting(DocId("first"), 3, 100),
      "term 2" -> Posting(DocId("second"), 3, 100)
    )

    val index = LocalInvertedIndex() :++ postings

    assert(index("term 1").nonEmpty)
    assert(index("term 2").nonEmpty)
  }

  it should "return a map with all terms and their associated postings" in {
    val index = LocalInvertedIndex().insertAll(
      Map(
        "term 1" -> Posting(DocId("first"), 3, 100),
        "term 2" -> Posting(DocId("second"), 3, 100)
      )
    )

    val result = index.getAll(List("term 1", "term 2"))

    assert(result("term 1").nonEmpty)
    assert(result("term 2").nonEmpty)
  }

  it should "return empty list when trying to get unknown keys" in {
    val result = LocalInvertedIndex().getAll(List("unknown"))

    assert(result("unknown").isEmpty)
  }

  it should "still return all known terms if another unknown key is given" in {
    val index = LocalInvertedIndex() :+ "known" -> Posting(DocId("known"), 3, 100)

    val result = index.getAll(List("known", "unknown"))

    assert(result.size === 2)
    assert(result("known").nonEmpty)
  }
}
