package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.domain.Tokenizer._
import htw.ai.p2p.speechsearch.domain.model.search.{FilterCriteria, Search}
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

  def extractDistinctTokens(search: Search): List[String] = {
    val queryTokens = (search.query.terms :: search.query.additions.map(_.terms))
      .flatMap(apply)
      .distinct
    val filterTokens = search.filter
      .filterNot(_.value.isBlank)
      .map(filter => buildFilterTerm(filter.criteria, filter.value))

    queryTokens ::: filterTokens
  }

}

object Tokenizer {

  def apply(): Tokenizer = new Tokenizer()

  def apply(stopWords: Set[String]) = new Tokenizer(stopWords)

  private val DelimiterPattern     = """(\W+-\W*|\W*-\W+|[\s,.?!:;\\`„‟'")(])+""" r
  private val NormalizationPattern = """[^\wäöüÄÖÜß]+""".r

  private val affiliationNorms: Map[String, String] =
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
        norm = "bündnis-90-die-grünen",
        variations = Set("die-grünen", "bündnis-90", "grüne")
      )
    ) flatMap (_ mappings) toMap

  def buildFilterTerm(criteria: FilterCriteria, value: String): String =
    s"${criteria.value}:${normalize(criteria, value.toLowerCase)}"

  private def normalize(criteria: FilterCriteria, filterTerm: String) = {
    val normalized = NormalizationPattern.replaceAllIn(filterTerm.toLowerCase, "-")
    if (criteria == Affiliation) affiliationNorms.getOrElse(normalized, normalized)
    else normalized
  }

  case class AffiliationNormalization(norm: String, variations: Set[String]) {
    def mappings: Set[(String, String)] = variations map (_ -> norm)
  }

}
