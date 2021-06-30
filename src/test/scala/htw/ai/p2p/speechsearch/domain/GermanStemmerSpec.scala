package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestUtils._
import org.scalatest.prop.TableDrivenPropertyChecks._

import java.nio.charset.{Charset, CharsetDecoder, CodingErrorAction}
import scala.io.Source.fromResource
import scala.util.Using

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class GermanStemmerSpec extends BaseShouldSpec {

  /**
   * test data: http://snowball.tartarus.org/algorithms/german/diffs.txt
   */
  private val TestDataFileName = "snowball_german_diffs.txt"
  private val TuplePattern     = """^(\S+)\s+(\S+)$""".r

  private val stemmedWords = Table(
    ("original", "stemmed"),
    readTestData: _*
  )

  "A German Stemmer" should "apply snowball stemming on given words" in {
    forAll(stemmedWords) { (original, stemmed) =>
      GermanStemmer(original) shouldBe stemmed
    }
  }

  it should "apply snowball stemming on single word" in {
    GermanStemmer("bundesfinanzminister") shouldBe "bundesfinanzminist"
  }

  "The GermanStemmerSpec" should "read the test data from file" in {
    readTestData should not be empty
  }

  private def readTestData: List[(String, String)] =
    Using(fromResource(TestDataFileName)) { y =>
      y.getLines().toList.map { case TuplePattern(a, b) => (a, b) }
    }.getOrFail(TestDataFileName)

  implicit val decoder: CharsetDecoder = Charset
    .forName("UTF-8")
    .newDecoder()
    .onMalformedInput(CodingErrorAction.REPORT)

}
