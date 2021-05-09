package htw.ai.p2p.speechsearch.service

import htw.ai.p2p.speechsearch.model.{DocId, Posting}
import munit.FunSuite

class InvertedIndexImplTest extends FunSuite {
  test("should insert postings when term is not yet known") {
    val postings = List(Posting(DocId("123"), List(1)))

    val index = InvertedIndexImpl().insert("test", postings)

    val result = index.get("test")
    assert(result.contains(postings), s"$result does not contain $postings")
  }

  test("should also insert with infix alias") {
    val postings = List(Posting(DocId("123"), List(1)))

    val index: InvertedIndex = InvertedIndexImpl() :+ ("test" -> postings)

    val result = index.get("test")
    assert(result.contains(postings), s"$result does not contain $postings")
  }

  test("should also get with apply alias") {
    val postings = List(Posting(DocId("123"), List(1)))
    val index: InvertedIndex = InvertedIndexImpl().insert("test", postings)

    val result = index("test")

    assert(result.contains(postings), s"$result does not contain $postings")
  }

  test("should append postings when term is already present") {
    val knownPosting = Posting(DocId("known"), List(1))
    val newPosting = Posting(DocId("unknown"), List(5, 7, 8))
    val index = InvertedIndexImpl().insert("test", List(knownPosting))

    val newIndex = index.insert("test", List(newPosting))

    val result = newIndex.get("test")
    assert(result.isDefined)
    assert(
      result.get.contains(newPosting),
      s"${result.get} does not contain $newPosting"
    )
    assert(
      result.get.contains(knownPosting),
      s"${result.get} does not contain $knownPosting"
    )
  }

  test("should insert all postings when term is not yet known") {
    val mappedPostings = Map(
      "term 1" -> List(Posting(DocId("first"), List(1, 3, 5))),
      "term 2" -> List(Posting(DocId("second"), List(2, 4, 6)))
    )

    val index = InvertedIndexImpl().insertAll(mappedPostings)

    assert(index("term 1").contains(mappedPostings("term 1")))
    assert(index("term 2").contains(mappedPostings("term 2")))
  }

  test("should append all postings when term is already present") {
    val knownPosting = Posting(DocId("known"), List(100))
    val newPosting = Posting(DocId("first"), List(1, 3, 5))
    val newPostings = Map(
      "term 1" -> List(newPosting),
      "term 2" -> List(Posting(DocId("second"), List(2, 4, 6)))
    )
    val index = InvertedIndexImpl() :+ ("term 1" -> List(knownPosting))

    val newIndex = index.insertAll(newPostings)

    val result = newIndex("term 1")
    assert(result.isDefined)
    assert(
      result.get.contains(newPosting),
      s"${result.get} does not contain $newPosting"
    )
    assert(
      result.get.contains(knownPosting),
      s"${result.get} does not contain $knownPosting"
    )
  }

  test("should allow infix operator for insert all") {
    val postings = Map(
      "term 1" -> List(Posting(DocId("first"), List(1, 3, 5))),
      "term 2" -> List(Posting(DocId("second"), List(2, 4, 6)))
    )

    val index = InvertedIndexImpl() :++ postings

    assert(index("term 1").isDefined)
    assert(index("term 2").isDefined)
  }

  test(
    "get all should return a map with all terms and their associated postings"
  ) {
    val index = InvertedIndexImpl().insertAll(
      Map(
        "term 1" -> List(Posting(DocId("first"), List(1, 3, 5))),
        "term 2" -> List(Posting(DocId("second"), List(2, 4, 6)))
      )
    )

    val result = index.getAll(List("term 1", "term 2"))

    assert(result("term 1").nonEmpty)
    assert(result("term 2").nonEmpty)
  }

  test("should return empty list when trying to get unknown keys") {
    val result = InvertedIndexImpl().getAll(List("unknown"))

    assert(result.isEmpty)
  }

  test("should also return all known terms if unknown key is given") {
    val index = InvertedIndexImpl() :+ ("known" -> List(
      Posting(DocId("known"), List(9, 8, 7))
    ))

    val result = index.getAll(List("known", "unknown"))

    assert(result.sizeIs == 1)
    assert(result("known").nonEmpty)
  }
}
