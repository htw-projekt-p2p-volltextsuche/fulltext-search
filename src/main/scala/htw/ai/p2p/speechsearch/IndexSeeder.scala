package htw.ai.p2p.speechsearch

import cats.effect.{Resource, Sync}
import cats.implicits._
import htw.ai.p2p.speechsearch.api.index.IndexService
import htw.ai.p2p.speechsearch.config.SpeechSearchConfig
import htw.ai.p2p.speechsearch.domain.model.speech.Speech
import io.chrisdavenport.log4cats.Logger
import io.circe.jawn.decode

import scala.io.Source.fromResource

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object IndexSeeder {

  def insertSpeeches[F[_]: Sync: Logger](
    indexService: IndexService[F],
    config: SpeechSearchConfig
  ): F[Unit] =
    if (config.index.insertSampleSpeeches)
      for {
        _ <-
          Logger[F].info(
            s"Reading sample speeches from file ${config.index.sampleSpeechesLocation}."
          )
        speeches <- readSpeeches(config)
        result   <- indexService.insert(speeches).attempt
        _ <-
          result fold (
            e => Logger[F].error(e)(s"Inserting sample speeches failed."),
            _ => Logger[F].info(s"Successfully inserted sample speeches.")
          )
        _ <-
          Logger[F].warn(
            s"The sample data should not be added when running in production. " +
              s"The insertion can be configured with 'index.insert-sample-speeches' config option."
          )
      } yield ()
    else Sync[F].unit

  private def readSpeeches[F[_]: Sync: Logger](
    config: SpeechSearchConfig
  ): F[List[Speech]] = {
    val fileName = config.index.sampleSpeechesLocation
    Resource
      .fromAutoCloseable(fromResource(fileName).pure[F])
      .use(source => decode[List[Speech]](source.getLines().mkString).liftTo[F])
      .handleErrorWith(
        Logger[F].error(_)(s"Reading sample data from '$fileName' failed.")
          *> List.empty[Speech].pure[F]
      )
  }

}
