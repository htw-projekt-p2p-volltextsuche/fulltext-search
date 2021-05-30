package htw.ai.p2p.speechsearch

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
    Using(fromResource(fileName))(_.getLines.mkString)
      .flatMap(decode[Speech](_).toTry) match {
      case Success(content) => content
      case Failure(e) =>
        fail(s"Reading file '$fileName' as '$Speech' failed.", e)
    }
}
