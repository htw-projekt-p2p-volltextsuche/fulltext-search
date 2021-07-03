package htw.ai.p2p.speechsearch

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits.catsSyntaxApplicativeId
import htw.ai.p2p.speechsearch.api.index.IndexService
import htw.ai.p2p.speechsearch.api.searches.SearchService
import htw.ai.p2p.speechsearch.config.SpeechSearchConfig._
import htw.ai.p2p.speechsearch.config._
import htw.ai.p2p.speechsearch.domain.invertedindex._
import htw.ai.p2p.speechsearch.domain.model.speech.Speech
import htw.ai.p2p.speechsearch.domain.model.speech.Speech._
import htw.ai.p2p.speechsearch.domain.{Index, _}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.jawn._
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.http4s._
import retry.RetryDetails.{GivingUp, WillDelayAndRetry}
import retry.RetryPolicies._
import retry._
import retry.syntax.all._

import scala.concurrent.duration.DurationInt
import scala.io.Source.fromResource

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object SpeechSearchApp extends IOApp {

  implicit def unsafeLogger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  private val DhtRetryPolicy: RetryPolicy[IO] = limitRetriesByCumulativeDelay(
    threshold = 5.seconds,
    policy = fibonacciBackoff(200.millis)
  )

  override def run(args: List[String]): IO[ExitCode] =
    for {
      config <- IO(ConfigSource.default.loadOrThrow[SpeechSearchConfig])
      logger <- Slf4jLogger.create[IO]

      stopWords <- readStopWords(config.index.stopWordsLocation, logger)
      ii        <- createInvertedIndex(config)
      index      = Index(Tokenizer(stopWords), ii)

      seededIndex <- insertSpeeches(index, config, logger)

      indexRef <- Ref[IO].of(seededIndex)
      searches  = SearchService.impl[IO](indexRef)
      indexes   = IndexService.impl[IO](indexRef)

      exitCode <-
        SpeechSearchServer
          .stream[IO](config.server.port, searches, indexes, config.server.basePath)
          .compile
          .drain
          .as(ExitCode.Success)
    } yield exitCode

  private def createInvertedIndex(
    config: SpeechSearchConfig
  ): IO[InvertedIndex] =
    config.index.storage match {
      case Local => IO(LocalInvertedIndex())
      case Distributed =>
        IO(DistributedInvertedIndex(DHTClient(config.index.dhtUri)))
          .retryingOnAllErrors(
            policy = DhtRetryPolicy,
            onError = handleDhtConnectionErrors
          )
          .flatMap(
            Logger[IO].info(
              s"Successfully connected to P2P network via entrypoint '${config.index.dhtUri}"
            ) *> IO.pure(_)
          )
    }

  private def handleDhtConnectionErrors(e: Throwable, details: RetryDetails) =
    details match {
      case WillDelayAndRetry(_, retriesSoFar, _) =>
        Logger[IO].error(e)(
          s"Failed to connect to P2P network after $retriesSoFar tries. Scheduled another retry."
        )
      case GivingUp(totalRetries, _) =>
        Logger[IO].error(e)(
          s"Failed to connect to P2P network after $totalRetries. Giving up." +
            s"Please verify that 'index.dhtUri' is configured properly and the dht service is accessible."
        )
    }

  private def readStopWords(fileName: String, logger: Logger[IO]): IO[Set[String]] =
    Resource
      .fromAutoCloseable(IO(fromResource(fileName)))
      .use(source => IO(source.getLines().toSet))
      .handleErrorWith { e =>
        logger.error(e)(
          s"Reading stop words from file '$fileName' failed."
        ) *> IO.raiseError(e)
      }

  private def insertSpeeches(
    index: Index,
    config: SpeechSearchConfig,
    logger: Logger[IO]
  ): IO[Index] =
    if (config.index.insertSampleSpeeches)
      readSpeeches(config, logger) map (_.foldLeft(index)(_.index(_)))
    else IO(index)

  private def readSpeeches(
    config: SpeechSearchConfig,
    logger: Logger[IO]
  ): IO[List[Speech]] = {
    val fileName = config.index.sampleSpeechesLocation
    Resource
      .fromAutoCloseable(IO(fromResource(fileName)))
      .use { source =>
        logger.info(
          s"Sample speeches from file '$fileName' added to the index."
        ) *> logger.warn(
          s"The sample data should not be added in production. Make sure to use this option only in development!"
        ) *> IO.fromEither(decode[List[Speech]](source.getLines().mkString))
      }
      .handleErrorWith { e =>
        logger.warn(e)(
          s"Reading sample data from '$fileName' failed."
        ) *> IO.pure(Nil)
      }
  }

}
