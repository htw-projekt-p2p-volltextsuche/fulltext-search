package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestData.{
  TestInvertedIndex,
  ValidUuid1,
  ValidUuid2,
  ValidUuid3
}
import htw.ai.p2p.speechsearch.domain.model.speech._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class LocalInvertedIndexSpec extends BaseShouldSpec {

  behavior of "A local Inverted Index"

  it should "insert posting when term is not yet known" in {
    val posting = List(Posting(DocId(ValidUuid1), 1, 100))

    val result = for {
      ii        <- TestInvertedIndex
      _         <- ii.insert("test", posting)
      retrieved <- ii.get("test")
    } yield retrieved

    result.asserting { case (term, postings) =>
      term shouldBe "test"
      postings should contain theSameElementsAs posting
    }
  }

  it should "also insert with infix alias" in {
    val posting = List(Posting(DocId(ValidUuid1), 1, 100))

    val result = for {
      ii        <- TestInvertedIndex
      _         <- ii :+ ("test" -> posting)
      retrieved <- ii.get("test")
    } yield retrieved

    result.asserting { case (term, postings) =>
      term shouldBe "test"
      postings should contain theSameElementsAs posting
    }
  }

  it should "also get with apply alias" in {
    val posting = List(Posting(DocId(ValidUuid1), 1, 100))

    val result = for {
      ii        <- TestInvertedIndex
      _         <- ii :+ ("test" -> posting)
      retrieved <- ii("test")
    } yield retrieved

    result.asserting(_._2 should not be empty)
  }

  it should "append posting when term is already present" in {
    val posting    = List(Posting(DocId(ValidUuid1), 1, 100))
    val newPosting = List(Posting(DocId(ValidUuid2), 3, 100))

    val result = for {
      ii        <- TestInvertedIndex
      _         <- ii :+ ("test" -> posting)
      _         <- ii :+ ("test" -> newPosting)
      retrieved <- ii("test")
    } yield retrieved

    result.asserting(_._2 should contain theSameElementsAs posting ::: newPosting)
  }

  it should "insert all postings when term is not yet known" in {
    val mappedPostings = Map(
      "term 1" -> List(Posting(DocId(ValidUuid1), 3, 100)),
      "term 2" -> List(Posting(DocId(ValidUuid2), 3, 100))
    )

    val result = for {
      ii        <- TestInvertedIndex
      _         <- ii.insertAll(mappedPostings)
      retrieved <- ii("term 1")
    } yield retrieved

    result.asserting(_._2 should contain theSameElementsAs mappedPostings("term 1"))
  }

  it should "append all postings when terms are already present" in {
    val posting    = List(Posting(DocId(ValidUuid1), 1, 100))
    val newPosting = List(Posting(DocId(ValidUuid2), 2, 100))
    val newPostings = Map(
      "term 1" -> newPosting,
      "term 2" -> List(Posting(DocId(ValidUuid3), 3, 100))
    )

    val result = for {
      ii        <- TestInvertedIndex
      _         <- ii :+ ("term 1" -> posting)
      _         <- ii :++ newPostings
      retrieved <- ii("term 1")
    } yield retrieved

    result.asserting(_._2 should contain theSameElementsAs posting ::: newPosting)
  }

  it should "allow infix operator for insert all" in {
    val postings = Map(
      "term 1" -> List(Posting(DocId(ValidUuid1), 3, 100)),
      "term 2" -> List(Posting(DocId(ValidUuid2), 3, 100))
    )

    val result = for {
      ii   <- TestInvertedIndex
      _    <- ii :++ postings
      res1 <- ii("term 1")
      res2 <- ii("term 2")
    } yield (res1, res2)

    result.asserting { res =>
      res._1._2 should not be empty
      res._2._2 should not be empty
    }
  }

  it should "return a map with all terms and their associated postings" in {
    val postings = Map(
      "term 1" -> List(Posting(DocId(ValidUuid1), 3, 100)),
      "term 2" -> List(Posting(DocId(ValidUuid2), 3, 100))
    )

    val result = for {
      ii        <- TestInvertedIndex
      _         <- ii :++ postings
      retrieved <- ii getAll List("term 1", "term 2")
    } yield retrieved

    result.asserting { indexMap =>
      indexMap("term 1") should not be empty
      indexMap("term 2") should not be empty
    }
  }

  it should "return empty list when trying to get unknown keys" in {
    TestInvertedIndex
      .flatMap(_("unknown"))
      .asserting(_._2 shouldBe empty)
  }

  it should "also return all known terms if another unknown key is given" in {
    val posting = List(Posting(DocId(ValidUuid1), 3, 100))

    val result = for {
      ii        <- TestInvertedIndex
      _         <- ii :+ ("known" -> posting)
      retrieved <- ii getAll List("known", "unknown")
    } yield retrieved

    result.asserting { indexMap =>
      indexMap.size shouldBe 2
      indexMap("known") should not be empty
      indexMap("unknown") shouldBe empty
    }
  }

}
