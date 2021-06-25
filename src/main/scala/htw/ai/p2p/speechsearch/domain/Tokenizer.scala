package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.domain.Tokenizer._
import htw.ai.p2p.speechsearch.domain.model.search.FilterCriteria
import htw.ai.p2p.speechsearch.domain.model.search.FilterCriteria.Affiliation
import org.slf4j.LoggerFactory

import scala.io.Source.fromResource
import scala.language.postfixOps
import scala.util._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class Tokenizer {

  def apply(text: String): List[String] =
    DelimiterPattern
      .split(text.toLowerCase)
      .view
      .filterNot(StopWords.getOrElse(Nil).contains)
      .filterNot(_.isBlank)
      .toList

}

object Tokenizer {

  private val DelimiterPattern         = """(\W+-\W*|\W*-\W+|[\s,.?!:;\\`„‟'")(])+""" r
  private val ImplicitDelimiterPattern = """(\W+-\W*|\W*-\W+|[^\w-])+""" r
  private val Logger                   = LoggerFactory.getLogger(getClass)

  private val stopWordsResourceName = "stopwords_de.txt"
  private val StopWords: Option[List[String]] =
    Using(fromResource(stopWordsResourceName))(_.getLines.toList) match {
      case Failure(e) =>
        // TODO: handle logging without side effects
        Logger.warn(s"Unable to read stop words from file $stopWordsResourceName", e)
        None
      case Success(v) => Some(v)
    }

  def apply(): Tokenizer = new Tokenizer()

  def buildFilterTerm(criteria: FilterCriteria, value: String): String =
    s"${criteria.value}:${normalize(criteria, value.toLowerCase)}"

  private val cduCsuSynonyms   = Set("cdu", "csu", "cdu/csu") map (_ -> "cdu/csu")
  private val dieLinkeSynonyms = Set("die-linke", "linke") map (_    -> "die-linke")
  private val dieGruenenSynonyms = Set(
    "die-grünen",
    "bündnis-90/die-grünen",
    "bündnis-90-die-grünen",
    "bündnis-90",
    "grüne"
  ) map (_ -> "die-grünen")
  private val affiliationNormMap: Map[String, String] =
    (cduCsuSynonyms ++ dieLinkeSynonyms ++ dieGruenenSynonyms).toMap

  private def normalize(criteria: FilterCriteria, filterTerm: String) = {
    val normalized = DelimiterPattern.replaceAllIn(filterTerm.toLowerCase, "-")
    if (criteria == Affiliation) affiliationNormMap.getOrElse(normalized, normalized)
    else normalized
  }

}
