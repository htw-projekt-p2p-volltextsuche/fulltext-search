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

class DHTClient {
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
    val postRequest = Request[IO](Method.POST, uri"https://localhost:8090/batch-post", headers = Headers.of(Header("Content-Type", "application/json")))
      .withEntity(s"""{"keys":[${terms.map(x => String.format("\"\"")).mkString(",")}]""")
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
