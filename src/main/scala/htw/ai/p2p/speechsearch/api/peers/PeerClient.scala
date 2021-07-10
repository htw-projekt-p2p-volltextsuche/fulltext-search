package htw.ai.p2p.speechsearch.api.peers

import cats.effect.{Async, ContextShift}
import cats.implicits._
import cats.{MonadError, Parallel}
import htw.ai.p2p.speechsearch.api.errors._
import htw.ai.p2p.speechsearch.api.peers.PeerClient.PeerResponse
import htw.ai.p2p.speechsearch.config.CirceConfig._
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex._
import io.circe.Json
import io.circe.generic.extras.auto._
import org.http4s.Method._
import org.http4s.Status.NotFound
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.{Client, ConnectionFailure, UnexpectedStatus}
import org.http4s.{Request, Response, Uri}

trait PeerClient[F[_]] {

  def getRaw(term: Term): F[Json]

  def getPosting(term: Term): F[(Term, PostingList)]

  def getPostings(terms: List[Term]): F[IndexMap]

  def insert(term: Term, postings: PostingList): F[Boolean]

  def insert(entries: IndexMap): F[Boolean]

}

object PeerClient {

  def apply[F[_]](implicit ev: PeerClient[F]): PeerClient[F] = ev

  sealed trait PeerResponse

  case class SuccessData(
    error: Boolean,
    key: String,
    value: Json = Json.Null
  ) extends PeerResponse
  case class PostingsData(
    error: Boolean = false,
    key: String,
    value: PostingList = Nil
  ) extends PeerResponse
  case class ErrorData(error: Boolean, errorMsg: String)

  sealed trait PeerRequest
  case class InsertionData(data: PostingList) extends PeerRequest

  def impl[F[_]: Async: ContextShift: Parallel](
    uri: Uri
  )(implicit M: MonadError[F, Throwable], C: Client[F]): PeerClient[F] =
    new PeerClient[F] with Http4sClientDsl[F] {

      override def getRaw(term: String): F[Json] = {
        val req = Request[F](GET, uri / term)
        C.expect[SuccessData](req)
          .adaptDomainError
          .map(_.value)
      }

      override def getPosting(term: String): F[(Term, PostingList)] =
        C.expectOption[PostingsData](Request(GET, uri / term))
          .recover { case UnexpectedStatus(NotFound) => None }
          .adaptDomainError
          .map(term -> _.fold[PostingList](Nil)(_.value))

      override def getPostings(terms: List[String]): F[IndexMap] =
        terms.parTraverse(getPosting).map(_.toMap)

      override def insert(term: String, postings: PostingList): F[Boolean] = {
        val req = Request[F](PUT, uri / "merge" / term)
          .withEntity(InsertionData(postings))
        C.expectOr[SuccessData](req) {
          _.as[ErrorData].map(e => PeerServerFailure(e.errorMsg))
        }.adaptDomainError
          .map(!_.error)
      }

      override def insert(entries: IndexMap): F[Boolean] =
        entries.toSeq.traverse { case (term, postings) =>
          insert(term, postings)
        } map (_ forall identity)

    }

  implicit class AdaptedClient[F[_]: Async, A](self: F[A]) {

    def adaptDomainError: F[A] = self.adaptError {
      case e: ConnectionFailure => PeerConnectionError(e)
      case e                    => PeerServerError(e)
    }

  }

  def test[F[_]]: PeerClient[F] = new PeerClient[F] {
    override def getRaw(term: String): F[Json] = ???

    override def getPosting(term: String): F[(Term, PostingList)] = ???

    override def getPostings(terms: List[String]): F[IndexMap] = ???

    override def insert(term: String, postings: PostingList): F[Boolean] = ???

    override def insert(entries: IndexMap): F[Boolean] = ???
  }

}
