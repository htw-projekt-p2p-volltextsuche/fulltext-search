package htw.ai.p2p.speechsearch

import htw.ai.p2p.speechsearch.api.indexes.SpeechData
import htw.ai.p2p.speechsearch.domain.model.Speech
import io.circe.generic.auto._
import io.circe.jawn.decode
import org.scalatest.Assertions.fail

import scala.io.Source.fromResource
import scala.util.{Failure, Success, Using}

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
object TestUtils {

  def readSpeechFromFile(fileName: String): Speech =
    readSpeechDataFromFile(fileName).toDomain

  def readSpeechDataFromFile(fileName: String): SpeechData =
    Using(fromResource(fileName))(_.getLines.mkString)
      .flatMap(decode[SpeechData](_).toTry) match {
      case Success(content) => content
      case Failure(e) =>
        fail(s"Reading file '$fileName' as '$SpeechData' failed.", e)
    }
}
