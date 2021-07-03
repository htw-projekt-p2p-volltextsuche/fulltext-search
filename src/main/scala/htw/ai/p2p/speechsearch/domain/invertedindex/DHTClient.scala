package htw.ai.p2p.speechsearch.domain.invertedindex

import cats.effect.{IO, _}
import htw.ai.p2p.speechsearch.domain.invertedindex.responseDataTypes.{
  PutResponse,
  ResponseDTO,
  ResponseMapDTO
}
import htw.ai.p2p.speechsearch.domain.model.speech.Posting
import io.circe.Json
import io.circe.literal._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.{Client, JavaNetClientBuilder}

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.ExecutionContext.Implicits.global

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

class DHTClientTest extends DHTClient {
  override def get(key: String): String =
    """{"error":false,"key":"test","value":[{"doc_id":"b272829e-f15b-44fe-8c25-25e3eff45300","tf":1,"doc_len":100},
      |{"doc_id":"32271a61-5c42-452f-8d59-7a2323c88bff","tf":2,"doc_len":10}]}""".stripMargin

  override def getMany(terms: List[String]): String =
    if (terms == List("linux", "windows"))
      """{
        |  "error": false,
        |  "keys": [
        |    "linux",
        |    "windows"
        |  ],
        |  "values": {
        |      "linux": {
        |      "error": false,
        |      "value": [
        |      {"doc_id":"b272829e-f15b-44fe-8c25-25e3eff45300","tf":1,"doc_len":100},
        |      {"doc_id":"32271a61-5c42-452f-8d59-7a2323c88bff","tf":2,"doc_len":10}
        |      ]
        |    },
        |    "windows": {
        |      "error": true,
        |      "value": [
        |      {"doc_id":"b272829e-f15b-44fe-8c25-25e3eff45300","tf":1,"doc_len":100},
        |      {"doc_id":"32271a61-5c42-452f-8d59-7a2323c88bff","tf":2,"doc_len":10}
        |      ]
        |    }
        |  }
        |}""".stripMargin
    else if (terms == List("testHasResults", "testNoResults"))
      """{
        |  "error": false,
        |  "keys": [
        |    "testHasResults",
        |    "testNoResults"
        |  ],
        |  "values": {
        |      "testHasResults": {
        |      "error": false,
        |      "value": [
        |      {"doc_id":"b272829e-f15b-44fe-8c25-25e3eff45300","tf":1,"doc_len":100},
        |      {"doc_id":"32271a61-5c42-452f-8d59-7a2323c88bff","tf":2,"doc_len":10}
        |      ]
        |    },
        |    "testNoResults": {
        |      "error": true,
        |      "errorMsg" : "Not found"
        |    }
        |  }
        |}""".stripMargin
    else "{}"

  override def post(key: String, data: Posting): Unit = ???

  override def postMany(entries: Map[String, Posting]): Unit = ???
}

class DHTClientProduction(uri: Uri) extends DHTClient {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  val blockingPool: ExecutorService = Executors.newFixedThreadPool(5)
  val blocker: Blocker              = Blocker.liftExecutorService(blockingPool)
  val httpClient: Client[IO]        = JavaNetClientBuilder[IO](blocker).create

  def get(key: String): String = {
    val getRequest = Request[IO](Method.GET, uri / key)
    val response = httpClient
      .run(getRequest)
      .use {
        case Status.Successful(r) =>
          r.attemptAs[ResponseDTO].leftMap(_.message).value
        case r =>
          r.as[Json].map(_ => Left(s"Request failed with status ${r.status.code}"))
      }
      .unsafeRunSync()
    response.fold(
      _ => json"""{ "error": false,"key": "linux","value": []}""".toString(),
      _.asJson.toString()
    )
  }

  def getMany(terms: List[String]): String = {
    val postRequest = Request[IO](Method.POST, uri / "batch-get")
      .withEntity(json"""{"keys":${terms.asJson}}""")
    val response = httpClient.expect[ResponseMapDTO](postRequest).unsafeRunSync()
    response.asJson.toString()
  }

  def post(key: String, data: Posting): Unit = {
    val postRequest =
      Request[IO](
        Method.PUT,
        uri / key,
        headers = Headers.of(Header("Content-Type", "application/json"))
      )
        .withEntity(json"""{"data":${data.asJson}}""")
    httpClient.expect[PutResponse](postRequest).unsafeRunSync()
  }

  def postMany(entries: Map[String, Posting]): Unit =
    for ((term, posting) <- entries) post(term, posting)
}
