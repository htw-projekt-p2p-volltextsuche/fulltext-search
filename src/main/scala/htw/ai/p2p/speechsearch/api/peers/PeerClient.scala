package htw.ai.p2p.speechsearch.api.peers

import cats.effect.{Async, ContextShift}
import cats.implicits._
import cats.{MonadError, Parallel}
import htw.ai.p2p.speechsearch.api.errors._
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
import org.http4s.{Request, Uri}

trait PeerClient[F[_]] {

  def getRaw(term: Term): F[Json]

  def getPosting(term: Term): F[(Term, PostingList)]

  def getPostings(terms: List[Term]): F[IndexMap]

  def insert(term: Term, postings: PostingList): F[Boolean]

  def insert(entries: IndexMap): F[Boolean]

}

object PeerClient {

  def apply[F[_]](implicit ev: PeerClient[F]): PeerClient[F] = ev

  def impl[F[_]: Async: ContextShift: Parallel: Client](
    uri: Uri
  )(implicit M: MonadError[F, Throwable]): PeerClient[F] =
    new PeerClient[F] with Http4sClientDsl[F] {

      private val client = implicitly[Client[F]]

      override def getRaw(term: String): F[Json] = {
        val req = Request[F](GET, uri / term)
        client
          .expect[SuccessData](req)
          .map(_.value)
          .adaptDomainError
      }

      override def getPosting(term: String): F[(Term, PostingList)] =
        client
          .expectOption[PostingsData](Request(GET, uri / term))
          .recover { case UnexpectedStatus(NotFound) => None }
          .map(term -> _.fold[PostingList](Nil)(_.value))
          .adaptDomainError

      override def getPostings(terms: List[String]): F[IndexMap] =
        terms.parTraverse(getPosting).map(_.toMap)

      override def insert(term: String, postings: PostingList): F[Boolean] = {
        val req = Request[F](PUT, uri / "merge" / term)
          .withEntity(InsertionData(postings))
        client
          .expectOr[SuccessData](req) {
            _.as[ErrorData].map(e => PeerServerFailure(e.errorMsg))
          }
          .map(!_.error)
          .adaptDomainError
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

}
