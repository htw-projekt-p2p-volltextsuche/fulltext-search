package htw.ai.p2p.speechsearch

import cats.effect._
import cats.effect.concurrent.Ref
import htw.ai.p2p.speechsearch.api.index.IndexService
import htw.ai.p2p.speechsearch.api.searches.SearchService
import htw.ai.p2p.speechsearch.domain._
import htw.ai.p2p.speechsearch.domain.invertedindex._
import htw.ai.p2p.speechsearch.domain.model.speech.Speech
import htw.ai.p2p.speechsearch.domain.model.speech.Speech._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.jawn._

import scala.io.Source.fromResource

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object SpeechSearchApp extends IOApp {

  implicit def unsafeLogger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  private val PortEnv               = "HTTP_PORT"
  private val DefaultPort           = 8421
  private val ApiPrefix             = "/api"
  private val StopWordsResourceName = "stopwords_de.txt"

  override def run(args: List[String]): IO[ExitCode] = {
    val port = args.headOption
      .orElse(sys.env.get(PortEnv))
      .fold(DefaultPort)(_.toInt)

    for {
      logger       <- Slf4jLogger.create[IO]
      stopWords    <- readStopWords(StopWordsResourceName, logger)
      tokenizer     = Tokenizer(stopWords)
      invertedIndex = DistributedInvertedIndex()
      index         = Index(tokenizer, invertedIndex)

      samples    <- readSpeeches("sample_data.json", logger)
      seededIndex = samples.foldLeft(index)(_.index(_))

      indexRef <- Ref[IO].of(seededIndex)
      searches  = SearchService.impl[IO](indexRef)
      indexes   = IndexService.impl[IO](indexRef)

      exitCode <-
        SpeechSearchServer
          .stream[IO](port, searches, indexes, ApiPrefix)
          .compile
          .drain
          .as(ExitCode.Success)
    } yield exitCode
  }

  def readStopWords(fileName: String, logger: Logger[IO]): IO[Set[String]] =
    Resource
      .fromAutoCloseable(IO(fromResource(StopWordsResourceName)))
      .use(source => IO(source.getLines().toSet))
      .handleErrorWith { e =>
        logger.error(e)(
          s"Reading stop words from file '$fileName' failed."
        ) *> IO.raiseError(e)
      }

  // TODO: remove seeding
  def readSpeeches(fileName: String, logger: Logger[IO]): IO[List[Speech]] =
    Resource
      .fromAutoCloseable(IO(fromResource(fileName)))
      .use { source =>
        IO.fromEither(decode[List[Speech]](source.getLines().mkString))
      }
      .handleErrorWith { e =>
        logger.warn(e)(
          s"Reading sample data from '$fileName' failed."
        ) *> IO.pure(Nil)
      }

}
