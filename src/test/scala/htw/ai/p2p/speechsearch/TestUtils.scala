package htw.ai.p2p.speechsearch

import htw.ai.p2p.speechsearch.api.searches.PaginatedSearch
import htw.ai.p2p.speechsearch.domain.core.GermanStemmer
import htw.ai.p2p.speechsearch.domain.core.model.result.SearchResult
import htw.ai.p2p.speechsearch.domain.core.model.search.Search
import htw.ai.p2p.speechsearch.domain.core.model.speech.{DocId, Speech}
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

  def readSpeechesFromFile(fileName: String): List[Speech] =
    readEntityFromFile(fileName)(decode[List[Speech]])

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

  implicit class TestSearch(self: Search) {

    def paginated: PaginatedSearch = PaginatedSearch(self)

  }

}
