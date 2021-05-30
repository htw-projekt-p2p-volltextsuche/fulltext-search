package htw.ai.p2p.speechsearch

import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}
import htw.ai.p2p.speechsearch.api.service.{Indexes, Searches}
import htw.ai.p2p.speechsearch.domain.{Index, LocalInvertedIndex, Tokenizer}

/**
  * @author Joscha Seelig <jduesentrieb> 2021
 **/
object SpeechSearchApp extends IOApp {

  private val PortEnv = "PORT"
  private val DefaultPort = 8080
  private val ApiPrefix = "/api"

  override def run(args: List[String]): IO[ExitCode] = {
    val port = args.headOption
      .orElse(sys.env.get(PortEnv))
      .fold(DefaultPort)(_.toInt)

    val index = Index(Tokenizer(), LocalInvertedIndex())

    for {
      indexRef <- Ref[IO].of(index)
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
}
