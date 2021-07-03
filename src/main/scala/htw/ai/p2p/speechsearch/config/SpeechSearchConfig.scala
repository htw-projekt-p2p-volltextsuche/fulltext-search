package htw.ai.p2p.speechsearch.config

import org.http4s.Uri
import org.http4s.implicits.http4sLiteralsSyntax
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveEnumerationReader
import pureconfig.generic.auto._
import pureconfig.module.http4s._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
case class SpeechSearchConfig(
  server: Server = Server(),
  index: Index = Index()
)

object SpeechSearchConfig {

  implicit val indexStorageConverter: ConfigReader[IndexStorage] =
    deriveEnumerationReader

}

sealed trait IndexStorage
case object Local       extends IndexStorage
case object Distributed extends IndexStorage

case class Server(
  port: Int = 8421,
  host: String = "0.0.0.0",
  basePath: String = "/api"
)

case class Index(
  storage: IndexStorage = Local,
  dhtUri: Uri = uri"http://localhost:8090/",
  stopWordsLocation: String = "stopwords_de.txt",
  insertSampleSpeeches: Boolean = true,
  sampleSpeechesLocation: String = "sample_speeches.json"
)
