package htw.ai.p2p.speechsearch.domain.invertedindex

import cats.effect.IO
import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.SpeechSearchServer.unsafeLogger
import htw.ai.p2p.speechsearch.TestData._
import htw.ai.p2p.speechsearch.api.peers.{PeerClient, PostingsData}
import htw.ai.p2p.speechsearch.domain.model.speech.{DocId, Posting}
import org.http4s.Method.GET
import org.http4s.Request
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.OneInstancePerTest

import scala.concurrent.duration.DurationInt

class DistributedInvertedIndexSpec
    extends BaseShouldSpec
    with OneInstancePerTest
    with AsyncMockFactory {

  implicit val mockClient: Client[IO] = mock[Client[IO]]

  private val peerClient: PeerClient[IO] =
    PeerClient.impl[IO](uri"/api", 1.second, 100.millis)
  val dii: InvertedIndex[IO] = InvertedIndex.distributed[IO](peerClient)

  behavior of "A distributed inverted index"

  ignore should "get single posting list" in {

    val expectedPostings = List(
      Posting(DocId(ValidUuid1), 13, 100),
      Posting(DocId(ValidUuid2), 23, 100)
    )
    val mockData = PostingsData(error = false, "test", expectedPostings)
    val request  = Request(GET, uri"/api/merge/test")

    /* --- current bug in ScalaMock: https://github.com/paulbutcher/ScalaMock/issues/93 ---
    (mockClient.expect[PostingsData](_: Url)(_: EntityDecoder[IO, PostingsData]))
       .expects(request, *)
       .returning(IO(mockData))
     */

    dii.get("test").asserting {
      _._2 should contain allElementsOf List(
        Posting(DocId(ValidUuid1), 13, 100),
        Posting(DocId(ValidUuid2), 23, 100)
      )
    }
  }

  ignore should "get multiple posting list" in {
    val getResult = dii.getAll(List[String]("linux", "windows"))
    getResult.asserting {
      _ should contain allElementsOf Map(
        "linux" -> List(
          Posting(DocId(ValidUuid1), 1, 100),
          Posting(DocId(ValidUuid2), 2, 10)
        ),
        "windows" -> List(
          Posting(DocId(ValidUuid1), 1, 100),
          Posting(DocId(ValidUuid2), 2, 10)
        )
      )
    }
  }

  ignore should "be able to get multiple posting list even if some keys have no results" in {
    val getResult = dii.getAll(List[String]("testHasResults", "testNoResults"))
    getResult.asserting {
      _ should contain allElementsOf Map(
        "testHasResults" -> List(
          Posting(DocId(ValidUuid1), 1, 100),
          Posting(DocId(ValidUuid2), 2, 10)
        ),
        "testNoResults" -> List()
      )
    }
  }

  // These test require the p2p network to be running
  val diiProd: InvertedIndex[IO] = dii

  behavior of "A distributed index with production client"

  ignore should "put new data" in {
    diiProd.insert("insertTerm", List(Posting(DocId(ValidUuid2), 2, 10)))
    val getResult = diiProd.get("insertTerm")
    getResult.asserting {
      _._2 should contain only Posting(DocId(ValidUuid2), 2, 10)
    }

  }

  ignore should "put multiple new data" in {
    diiProd.insertAll(
      Map(
        "insertMultipleTerm1" -> List(Posting(DocId(ValidUuid2), 2, 10)),
        "insertMultipleTerm2" -> List(Posting(DocId(ValidUuid1), 1, 10))
      )
    )
    val getResult =
      diiProd.getAll(List("insertMultipleTerm1", "insertMultipleTerm2"))
    getResult.asserting { results =>
      results("insertMultipleTerm1") should contain(
        Posting(DocId(ValidUuid2), 2, 10)
      )
      results("insertMultipleTerm2") should contain(
        Posting(DocId(ValidUuid1), 1, 10)
      )
    }
  }

  ignore should "get single posting list" in {
    val getResult = diiProd.get("notFindable")
    getResult asserting { _._2 shouldBe empty }
  }

  ignore should "get single posting list 2" in {
    val getResult = diiProd.get("testDoc")
    getResult asserting {
      _._2 should contain only Posting(DocId(ValidUuid1), 1, 100)
    }
  }

  ignore should "get posting list for multiple keys" in {
    val getResult = diiProd.getAll(List("testDoc", "empty"))
    getResult asserting {
      _ should contain allElementsOf Map(
        "testDoc" -> List(Posting(DocId(ValidUuid1), 1, 100)),
        "empty"   -> Nil
      )
    }
  }

}
