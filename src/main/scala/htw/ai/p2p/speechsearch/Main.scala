package htw.ai.p2p.speechsearch

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    SpeechSearchServer.stream[IO].compile.drain.as(ExitCode.Success)
}
