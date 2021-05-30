package htw.ai.p2p.speechsearch.domain

import scala.io.Source.fromResource
import scala.util.{Failure, Success, Using}

/**
  * @author Joscha Seelig <jduesentrieb> 2021
 **/
object Tokenizer {
  def apply(): Tokenizer = new Tokenizer()
}

class Tokenizer {

  private val DelimiterPattern = """[\s,.?!:'"]"""

  private val stopWords = readStopWords

  def apply(text: String): List[String] =
    text.toLowerCase
      .split(DelimiterPattern)
      .filterNot(stopWords.contains)
      .filter(!_.isBlank)
      .toList

  private def readStopWords: List[String] =
    Using(fromResource("stopwords_de.txt"))(_.getLines.toList)
//     .fold(throw new IllegalStateException) ()
    match {
      case Success(content) => content
      case Failure(e)       => throw new IllegalStateException(e)
    }

}
