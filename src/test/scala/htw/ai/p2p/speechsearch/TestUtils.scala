package htw.ai.p2p.speechsearch

import htw.ai.p2p.speechsearch.domain.model.search.Search
import htw.ai.p2p.speechsearch.domain.model.speech.Speech
import io.circe._
import io.circe.jawn.decode
import org.scalatest.Assertions.fail

import scala.io.Source.fromResource
import scala.language.implicitConversions
import scala.util._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object TestUtils {

  def readSpeechFromFile(fileName: String): Speech =
    readEntityFromFile(fileName)(decode[Speech])

  def readSearchFromFile(fileName: String): Search =
    readEntityFromFile(fileName)(decode[Search])

  def readFile(fileName: String): String =
    Using(fromResource(fileName))(_.getLines() mkString).getOrFail(fileName)

  def readLineSetFromFile(fileName: String): Set[String] =
    Using(fromResource(fileName))(_.getLines() toSet).getOrFail(fileName)

  private def readEntityFromFile[A](
                                     fileName: String
                                   )(decode: String => Either[Error, A]): A =
    Using(fromResource(fileName))(_.getLines() mkString)
      .flatMap(decode(_).toTry)
      .getOrFail(fileName)

  implicit class TestTry[A](t: Try[A]) {
    def getOrFail(fileName: String): A =
      t.fold(
        e => fail(s"Reading file '$fileName' as entity failed.", e),
        identity
      )
  }
}
