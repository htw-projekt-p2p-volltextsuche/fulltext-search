package htw.ai.p2p.speechsearch

import cats.effect._
import cats.effect.concurrent.Ref
import htw.ai.p2p.speechsearch.api.indexes.Indexes
import htw.ai.p2p.speechsearch.api.searches.Searches
import htw.ai.p2p.speechsearch.domain._
import htw.ai.p2p.speechsearch.domain.invertedindex.LocalInvertedIndex
import htw.ai.p2p.speechsearch.domain.model.speech.{DocId, Speech}
import io.circe.jawn

import scala.io.Source.fromResource
import scala.util.Using

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object SpeechSearchApp extends IOApp {

  private val PortEnv = "PORT"
  private val DefaultPort = 8421
  private val ApiPrefix = "/api"

  override def run(args: List[String]): IO[ExitCode] = {
    val port = args.headOption
      .orElse(sys.env.get(PortEnv))
      .fold(DefaultPort)(_.toInt)

    val index = Index(Tokenizer(), LocalInvertedIndex())

    for {
      samples <- readSpeeches("sample_data.json")
      seededIndex = samples.foldLeft(index)(_.index(_))

      indexRef <- Ref[IO].of(seededIndex)
      searches = Searches.impl[IO](indexRef)
      indexes = Indexes.impl[IO](indexRef)
      exitCode <-
        SpeechSearchServer
          .stream[IO](port, searches, indexes, ApiPrefix)
          .compile
          .drain
          .as(ExitCode.Success)
    } yield exitCode
  }

  // TODO: remove seeding
  def readSpeeches(fileName: String): IO[List[Speech]] = IO {
    Using(fromResource(fileName))(_.getLines.mkString)
      .flatMap(jawn.decode[List[Speech]](_).toTry)
      .get
      .zipWithIndex
      .map(s => s._1.copy(DocId(s._2.toString)))
  }

}
