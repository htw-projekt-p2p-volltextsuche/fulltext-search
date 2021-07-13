package htw.ai.p2p.speechsearch

import cats.Parallel
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import com.olegpy.meow.hierarchy._
import htw.ai.p2p.speechsearch.IndexSeeder.insertSpeeches
import htw.ai.p2p.speechsearch.api.errors._
import htw.ai.p2p.speechsearch.api.index.{IndexRoutes, IndexService}
import htw.ai.p2p.speechsearch.api.peers.PeerClient
import htw.ai.p2p.speechsearch.api.searches.{SearchRoutes, SearchService}
import htw.ai.p2p.speechsearch.config.SpeechSearchConfig._
import htw.ai.p2p.speechsearch.config._
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex.{PostingList, Term}
import htw.ai.p2p.speechsearch.domain.{Indexer, Searcher, Tokenizer}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.{Logger => ClientLogger}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, Logger => ServerLogger}
import org.http4s.server.{Router, Server}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.http4s._

import scala.concurrent.ExecutionContext.global
import scala.io.Source.fromResource

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object SpeechSearchServer extends IOApp {

  implicit def unsafeLogger[F[_]: Sync]: Logger[F] =
    Slf4jLogger.getLogger[F]

  def serve[F[_]: ContextShift: ConcurrentEffect: Timer: Parallel]
    : Resource[F, Server[F]] =
    for {
      config <- Resource.pure[F, SpeechSearchConfig](
                  ConfigSource.default.loadOrThrow[SpeechSearchConfig]
                )
      stopWords   <- Resource.eval(readStopWords(config.index.stopWordsLocation))
      tokenizer    = Tokenizer(stopWords)
      indexer      = Indexer(tokenizer)
      ii          <- initInvertedIndex[F](config)
      indexService = IndexService.impl(indexer, ii)

      searcher      = Searcher(tokenizer)
      searchService = SearchService.impl(searcher, ii)

      implicit0(eh: HttpErrorHandler[F, ApiError]) <-
        Resource.pure[F, HttpErrorHandler[F, ApiError]](new ApiErrorHandler)

      services = SearchRoutes.routes(searchService) <+>
                   IndexRoutes.routes(indexService)

      httpApp = Router("/api" -> services).orNotFound
      corsApp = CORS(httpApp, config.server.corsPolicy)
      loggingHttpApp = ServerLogger.httpApp(
                         logHeaders = true,
                         logBody = config.server.logBody
                       )(corsApp)

      server <- BlazeServerBuilder(global)
                  .bindHttp(config.server.port, config.server.host)
                  .withHttpApp(loggingHttpApp)
                  .resource

      _ <- Resource.eval(insertSpeeches(indexService, config))
    } yield server

  private def readStopWords[F[_]: Sync: Logger](
    fileName: String
  ): F[Set[String]] =
    Resource
      .fromAutoCloseable(fromResource(fileName).pure[F])
      .use(_.getLines().toSet.pure[F])
      .handleErrorWith { e =>
        Logger[F].error(e)(
          s"Reading stop words from file '$fileName' failed."
        ) *> Sync[F].raiseError(e)
      }

  private def initInvertedIndex[
    F[_]: ContextShift: ConcurrentEffect: Parallel: Timer: Logger
  ](config: SpeechSearchConfig): Resource[F, InvertedIndex[F]] =
    Resource.eval(
      Logger[F].info(
        s"Initialize inverted index with storage policy: ${config.index.storage}"
      )
    ) *> {
      config.index.storage match {
        case Local           => initLocalInvertedIndex
        case Distributed     => initDistributedInvertedIndex(config)
        case LazyDistributed => initLazyDistributedInvertedIndex(config)
      }
    }

  def initLocalInvertedIndex[F[_]: ConcurrentEffect]: Resource[F, InvertedIndex[F]] =
    for {
      indexRef <- Resource.eval(Ref[F].of(Map.empty[Term, PostingList]))
    } yield InvertedIndex.local(indexRef)

  def initDistributedInvertedIndex[
    F[_]: ContextShift: ConcurrentEffect: Parallel: Timer
  ](config: SpeechSearchConfig): Resource[F, InvertedIndex[F]] =
    for {
      peerClient <- initPeerClient(config)
    } yield InvertedIndex.distributed(peerClient)

  def initLazyDistributedInvertedIndex[
    F[_]: ContextShift: ConcurrentEffect: Parallel: Timer
  ](config: SpeechSearchConfig): Resource[F, InvertedIndex[F]] =
    for {
      indexRef   <- Resource.eval(Ref[F].of(Map.empty[Term, PostingList]))
      peerClient <- initPeerClient(config)
      ii = InvertedIndex.lazyDistributed(
             indexRef,
             peerClient,
             config.index.distributionInterval,
             config.index.distributionChunkSize
           )
      _ <- Resource.eval(ii.run)
    } yield ii

  private def initPeerClient[
    F[_]: ConcurrentEffect: ContextShift: Timer: Parallel
  ](config: SpeechSearchConfig): Resource[F, PeerClient[F]] =
    for {
      implicit0(c: Client[F]) <- makeClient(config)
    } yield PeerClient.impl(
      config.peers.uri,
      config.peers.retryThreshold,
      config.peers.retryBackoff
    )

  private def makeClient[F[_]: ConcurrentEffect: Timer](
    config: SpeechSearchConfig
  ): Resource[F, Client[F]] =
    BlazeClientBuilder(global)
      .withRequestTimeout(config.peers.requestTimeout)
      .withConnectTimeout(config.peers.connectTimeout)
      .withChunkBufferMaxSize(config.peers.chunkBufferMaxSize)
      .withBufferSize(config.peers.bufferSize)
      .withMaxWaitQueueLimit(config.peers.maxWaitQueueLimit)
      .resource
      .map(ClientLogger(logHeaders = true, logBody = config.peers.logBody)(_))

  override def run(args: List[String]): IO[ExitCode] =
    serve.use(_ => IO.never).as(ExitCode.Success)

}
