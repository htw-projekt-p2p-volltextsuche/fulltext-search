package htw.ai.p2p.speechsearch

import htw.ai.p2p.speechsearch.domain.GermanStemmer
import htw.ai.p2p.speechsearch.domain.model.result.SearchResult
import htw.ai.p2p.speechsearch.domain.model.search.Search
import htw.ai.p2p.speechsearch.domain.model.speech.{DocId, Speech}
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
    Using(fromResource(fileName))(_.getLines() mkString) getOrFail fileName

  def readLineSetFromFile(fileName: String): Set[String] =
    Using(fromResource(fileName))(_.getLines() toSet) getOrFail fileName

  private def readEntityFromFile[A](
    fileName: String
  )(decode: String => Either[Error, A]): A =
    (Using(fromResource(fileName))(_.getLines() mkString)
      flatMap (decode(_).toTry)
      getOrFail fileName)

  implicit class TestTry[A](self: Try[A]) {

    def getOrFail(fileName: String): A =
      self.fold(
        e => fail(s"Reading file '$fileName' failed.", e),
        identity
      )

  }

  implicit class TestSearchResult(self: SearchResult) {

    def docIds: Seq[DocId] = self.results map (_.docId)

  }

  implicit class TestString(self: String) {

    def stemmed: String = GermanStemmer(self)

  }

}
