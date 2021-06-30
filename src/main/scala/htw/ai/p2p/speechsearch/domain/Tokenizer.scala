package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.domain.Tokenizer._
import htw.ai.p2p.speechsearch.domain.model.search.FilterCriteria
import htw.ai.p2p.speechsearch.domain.model.search.FilterCriteria.Affiliation

import scala.language.postfixOps

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class Tokenizer(stopWords: Set[String] = Set.empty) {

  def apply(text: String): List[String] =
    DelimiterPattern
      .split(text toLowerCase)
      .view
      .filterNot(stopWords contains)
      .filterNot(_ isBlank)
      .map(GermanStemmer(_))
      .toList

}

object Tokenizer {

  def apply(): Tokenizer = new Tokenizer()

  def apply(stopWords: Set[String]) = new Tokenizer(stopWords)

  private val DelimiterPattern = """(\W+-\W*|\W*-\W+|[\s,.?!:;\\`„‟'")(])+""" r

  private val affiliationNormMap: Map[String, String] =
    Set(
      AffiliationNormalization(
        norm = "cdu-csu",
        variations = Set("cdu", "csu")
      ),
      AffiliationNormalization(
        norm = "die-linke",
        variations = Set("linke")
      ),
      AffiliationNormalization(
        norm = "die-grünen",
        variations = Set("die-grünen", "bündnis-90", "grüne")
      )
    ) flatMap (_ mappings) toMap

  def buildFilterTerm(criteria: FilterCriteria, value: String): String =
    s"${criteria.value}:${normalize(criteria, value.toLowerCase)}"

  private val NormalizationPattern = """[^\wäöüÄÖÜß]+""".r

  private def normalize(criteria: FilterCriteria, filterTerm: String) = {
    val normalized = NormalizationPattern.replaceAllIn(filterTerm.toLowerCase, "-")
    if (criteria == Affiliation) affiliationNormMap.getOrElse(normalized, normalized)
    else normalized
  }

  case class AffiliationNormalization(norm: String, variations: Set[String]) {
    def mappings: Set[(String, String)] = variations map (_ -> norm)
  }

}
