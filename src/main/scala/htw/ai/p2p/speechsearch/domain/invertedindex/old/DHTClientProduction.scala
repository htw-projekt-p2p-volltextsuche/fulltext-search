package htw.ai.p2p.speechsearch.domain.invertedindex.old

import cats.effect._
import htw.ai.p2p.speechsearch.domain.invertedindex.old.responseDataTypes.{
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
import org.http4s.client._

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.ExecutionContext.Implicits.global

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
