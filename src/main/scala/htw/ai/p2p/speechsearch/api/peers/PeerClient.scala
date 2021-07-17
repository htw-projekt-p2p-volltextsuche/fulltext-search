package htw.ai.p2p.speechsearch.api.peers

import cats.effect.{Async, ContextShift, Timer}
import cats.implicits._
import cats.{MonadError, Parallel}
import htw.ai.p2p.speechsearch.api.errors._
import htw.ai.p2p.speechsearch.domain.ImplicitUtilities.FormalizedString
import htw.ai.p2p.speechsearch.domain.core.invertedindex.InvertedIndex._
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.auto._
import org.http4s.Method._
import org.http4s.Status.{NotFound, ServiceUnavailable}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.{Client, ConnectionFailure, UnexpectedStatus}
import org.http4s.{Request, Response, Uri}
import retry.RetryDetails.{GivingUp, WillDelayAndRetry}
import retry.RetryPolicies.{fibonacciBackoff, limitRetriesByCumulativeDelay}
import retry.{RetryDetails, Sleep}

import scala.concurrent.duration.FiniteDuration

trait PeerClient[F[_]] {

  def getIndexSize: F[Int]

  def getPosting(term: Term): F[(Term, PostingList)]

  def getPostings(terms: List[Term]): F[IndexMap]

  def insert(term: Term, postings: PostingList): F[Boolean]

  /**
   * Inserts all the specified entries into the p2p network
   * and returns eventually failed term-postings pairs.
   * Consequently an empty returned list indicates success
   * for all specified entries.
   *
   * @param entries Entries that are to be inserted.
   * @return All eventually failed term-postings pairs.
   */
  def insert(entries: IndexMap): F[IndexMap]

}

object PeerClient {

  def apply[F[_]](implicit ev: PeerClient[F]): PeerClient[F] = ev

  def impl[F[_]: Async: Parallel: Timer: ContextShift: Client: Logger](
    uri: Uri,
    retryThreshold: FiniteDuration,
    retryBackoff: FiniteDuration
  )(implicit M: MonadError[F, Throwable]): PeerClient[F] =
    new PeerClient[F] with Http4sClientDsl[F] {

      implicit val config: Configuration = Configuration.default.withDefaults

      private val client = implicitly[Client[F]]

      override def getIndexSize: F[Int] =
        client
          .expectOr[SuccessData](uri / IndexSizeKey)(mapPeerFailures)
          .flatMap(_.value.as[Int].liftTo[F])
          .retryOnAllErrors(retryThreshold, retryBackoff)
          .adaptDomainError

      override def getPosting(term: String): F[(Term, PostingList)] =
        client
          .expectOptionOr[PostingsData](Request(GET, uri / term))(mapPeerFailures)
          .recover { case UnexpectedStatus(NotFound) => None }
          .map(term -> _.fold[PostingList](Nil)(_.value))
          .retryOnAllErrors(retryThreshold, retryBackoff)
          .adaptDomainError

      override def getPostings(terms: List[String]): F[IndexMap] =
        terms.parTraverse(getPosting).map(_.toMap)

      override def insert(term: String, postings: PostingList): F[Boolean] = {
        val req =
          Request[F](PUT, uri / "merge" / term).withEntity(InsertionData(postings))
        client
          .expectOr[SuccessData](req)(mapPeerFailures)
          .map(!_.error)
          .retryOnAllErrors(retryThreshold, retryBackoff)
          .adaptDomainError
      }

      override def insert(entries: IndexMap): F[IndexMap] =
        entries.toList traverseOrCancel { case (term, postings) =>
          insert(term, postings)
            .map(_ => Success: ExecutionStatus)
            .recover {
              case _: PeerConnectionError    => FatalFail
              case _: PeerServiceUnavailable => FatalFail
              case _                         => Fail
            }
        } map (_.toMap)

      private def mapPeerFailures(response: Response[F]): F[Throwable] =
        response match {
          case ServiceUnavailable(_) =>
            (PeerServiceUnavailable(): Throwable).pure[F]
          case ServiceUnavailable(e) =>
            (PeerServiceUnavailable(e.body.toString()): Throwable).pure[F]
          case resp =>
            resp.as[ErrorData].map { e =>
              PeerServerFailure(s"Failure in P2P network: ${e.errorMsg}")
            }
        }

    }

  sealed trait ExecutionStatus
  case object Success   extends ExecutionStatus
  case object Fail      extends ExecutionStatus
  case object FatalFail extends ExecutionStatus

  implicit class CancelableTraverseList[A](self: List[A]) {

    def traverseOrCancel[F[_]: Async](f: A => F[ExecutionStatus]): F[List[A]] = {
      def go(rem: List[A], acc: F[List[A]] = List.empty.pure[F]): F[List[A]] =
        rem match {
          case Nil => acc
          case x :: xs =>
            f(x) >>= {
              case Success   => go(xs, acc)
              case Fail      => go(xs, acc.map(x :: _))
              case FatalFail => acc.map(_ ::: x :: xs)
            }
        }
      go(self)
    }

  }

  implicit class ErrorHandlingFunctor[F[_]: Async: Sleep: Logger, A](self: F[A]) {

    import retry.syntax.all._
    def adaptDomainError: F[A] = self.adaptError {
      case e: PeerError         => e
      case e: ConnectionFailure => PeerConnectionError(e)
      case e                    => PeerServerError(e)
    }

    def retryOnAllErrors(
      threshold: FiniteDuration,
      backoff: FiniteDuration
    ): F[A] = self.retryingOnAllErrors(
      policy = limitRetriesByCumulativeDelay(
        threshold = threshold,
        policy = fibonacciBackoff(backoff)
      ),
      onError = handlePeerConnectionErrors
    )

    private def handlePeerConnectionErrors(
      e: Throwable,
      details: RetryDetails
    ): F[Unit] =
      details match {
        case WillDelayAndRetry(_, retriesSoFar, _) =>
          Logger[F].error(e)(
            s"Failure in P2P network. Tried ${retriesSoFar + 1} " +
              s"${"time".formalized(retriesSoFar + 1)} yet. Scheduled another retry."
          )
        case GivingUp(totalRetries, _) =>
          Logger[F].error(e)(
            s"Failure in P2P network. Giving up after $totalRetries tries." +
              s"Please verify that 'index.dhtUri' is configured properly and the P2P service is accessible."
          )
      }

  }

}
