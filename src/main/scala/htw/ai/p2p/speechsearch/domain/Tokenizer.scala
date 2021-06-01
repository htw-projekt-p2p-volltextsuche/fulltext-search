package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.domain.Tokenizer.StopWords

import scala.io.Source.fromResource
import scala.util.Using

/**
  * @author Joscha Seelig <jduesentrieb> 2021
 **/
object Tokenizer {

  def apply(): Tokenizer = new Tokenizer()

  private val StopWords =
    Using(fromResource("stopwords_de.txt"))(_.getLines.toList)
      .fold(e => throw new IllegalStateException(e), identity)
}

class Tokenizer {

  private val DelimiterPattern = """[\s,.?!:'"]"""

  def apply(text: String): List[String] =
    text.toLowerCase
      .split(DelimiterPattern)
      .view
      .filterNot(StopWords.contains)
      .filterNot(_.isBlank)
      .toList
}
