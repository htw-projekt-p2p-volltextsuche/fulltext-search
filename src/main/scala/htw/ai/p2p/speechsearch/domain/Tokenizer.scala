package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.domain.Tokenizer._
import htw.ai.p2p.speechsearch.domain.model.search.FilterCriteria
import org.slf4j.LoggerFactory

import scala.io.Source.fromResource
import scala.util._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class Tokenizer {

  def apply(text: String): List[String] =
    text.toLowerCase
      .split(DelimiterPattern)
      .view
      .filterNot(StopWords.getOrElse(Nil).contains)
      .filterNot(_.isBlank)
      .toList

}

object Tokenizer {

  private val DelimiterPattern = """([\s,.?!:'")(]|\s-\s)+"""
  private val Logger = LoggerFactory.getLogger(getClass)

  private val stopWordsResourceName = "stopwords_de.txt"
  private val StopWords: Option[List[String]] = Using(fromResource(stopWordsResourceName))(_.getLines.toList) match {
    case Failure(e) =>
      // TODO: handle logging without side effects
      Logger.warn(s"Unable to read stop words from file $stopWordsResourceName", e)
      None
    case Success(v) => Some(v)
  }

  def apply(): Tokenizer = new Tokenizer()

  def constructFilterTerm(criteria: FilterCriteria, value: String): String =
    s"${criteria.value}:${value.toLowerCase.replaceAll(DelimiterPattern, "-")}"

}
