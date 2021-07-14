package htw.ai.p2p.speechsearch.config

import cats.implicits._
import org.http4s.Uri
import org.http4s.client.defaults
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.server.middleware.CORSConfig
import pureconfig.{ConfigReader, ConvertHelpers}
import pureconfig.generic.auto._
import pureconfig.generic.semiauto._
import pureconfig.module.http4s._

import scala.concurrent.duration.{DurationInt, FiniteDuration}

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
case class SpeechSearchConfig(
  server: Server = Server(),
  index: Index = Index(),
  search: Search = Search(),
  peers: Peers = Peers()
)

object SpeechSearchConfig {

  implicit val indexStorageConverter: ConfigReader[IndexStoragePolicy] =
    deriveEnumerationReader

  implicit val allowedOriginsConverter: ConfigReader[String => Boolean] =
    ConfigReader.fromString[String => Boolean] {
      ConvertHelpers.catchReadError(s =>
        (a: String) => if (a.matches(s)) true else false
      )
    }

}

sealed trait IndexStoragePolicy
case object Local           extends IndexStoragePolicy
case object Distributed     extends IndexStoragePolicy
case object LazyDistributed extends IndexStoragePolicy

case class Server(
  port: Int = 8421,
  host: String = "0.0.0.0",
  basePath: String = "/api",
  logBody: Boolean = true,
  corsPolicy: CORSConfig = CORSConfig(
    anyOrigin = true,
    allowCredentials = true,
    maxAge = 1.day.toSeconds,
    anyMethod = false,
    allowedMethods = Set("POST").some
  )
)

case class Index(
  storage: IndexStoragePolicy = Local,
  stopWordsLocation: String = "stopwords_de.txt",
  sampleSpeechesLocation: String = "sample_speeches.json",
  insertSampleSpeeches: Boolean = true,
  distributionInterval: FiniteDuration = 5.minutes,
  distributionChunkSize: Int = 100
)

case class Search(
  cacheSize: Int = 10
)

case class Peers(
  uri: Uri = uri"http://localhost:8090/",
  logBody: Boolean = true,
  retryThreshold: FiniteDuration = 1.second,
  retryBackoff: FiniteDuration = 200.millis,
  requestTimeout: FiniteDuration = defaults.RequestTimeout,
  connectTimeout: FiniteDuration = defaults.ConnectTimeout,
  chunkBufferMaxSize: Int = 1024 * 1024,
  bufferSize: Int = 16384,
  maxWaitQueueLimit: Int = 1024
)
