package htw.ai.p2p.speechsearch.domain.invertedindex

import java.util.concurrent.Executors

import cats.effect.{IO, _}
import htw.ai.p2p.speechsearch.domain.model.speech.Posting
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits._
import org.http4s._

import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global


sealed trait Resp
case class PostingList(body: String) extends Resp

trait DHTClient {
  def get(key: String): String

  def getMany(terms: List[String]): String

  def post(key: String, data: Posting)

  def postMany(entries: Map[String, Posting])
}

class DHTClientTest extends DHTClient {
  override def get(key: String): String = {
    """{"error":false,"key":"test","value":[{"doc_id":"b272829e-f15b-44fe-8c25-25e3eff45300","tf":1,"doc_len":100},
      |{"doc_id":"32271a61-5c42-452f-8d59-7a2323c88bff","tf":2,"doc_len":10}]}""".stripMargin
  }

  override def getMany(terms: List[String]): String = {
    if(terms == List("linux", "windows"))
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
    else if(terms == List("testHasResults", "testNoResults"))
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
  }

  override def post(key: String, data: Posting): Unit = ???

  override def postMany(entries: Map[String, Posting]): Unit = ???
}

class DHTClientProduction extends DHTClient {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  val blockingPool = Executors.newFixedThreadPool(5)
  val blocker = Blocker.liftExecutorService(blockingPool)
  val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create

  def get(key: String): String = {
    val getRequest = Request[IO](Method.GET, uri"https://localhost:8090/" / key)
    httpClient.expect[String](getRequest)
    httpClient.run(getRequest).use {
      case Status.Successful(r) => return r.body.toString();
      case _ => return "Request failed"
    }
    "Request failed"
  }

  def getMany(terms:  List[String]): String =  {
    val postRequest = Request[IO](Method.POST, uri"https://localhost:8090/batch-get", headers = Headers.of(Header("Content-Type", "application/json")))
      .withEntity(s"""{"keys":${terms.asJson}}""")
    httpClient.run(postRequest).use{
      case Status.Successful(r) => return r.body.toString();
      case _ => return "Request failed"
    }
    "Request failed"
  }

  def post(key: String, data: Posting) = {
    val postRequest =
      Request[IO](Method.POST, uri"https://localhost:8090/append/" / key, headers = Headers.of(Header("Content-Type", "application/json")))
        .withEntity(data.asJson)
    httpClient.run(postRequest)
  }

  def postMany(entries: Map[String, Posting]) = {
    for((term, posting) <- entries) post(term, posting)
  }
}
