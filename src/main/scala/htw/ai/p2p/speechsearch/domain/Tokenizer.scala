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
      .toList

}

object Tokenizer {

  def apply(): Tokenizer = new Tokenizer()

  def apply(stopWords: Set[String]) = new Tokenizer(stopWords)

  private val DelimiterPattern = """(\W+-\W*|\W*-\W+|[\s,.?!:;\\`„‟'")(])+""" r

  def buildFilterTerm(criteria: FilterCriteria, value: String): String =
    s"${criteria.value}:${normalize(criteria, value.toLowerCase)}"

  private val cduCsuSynonyms = Set("cdu", "csu", "cdu/csu") map (_ -> "cdu/csu")
  private val dieLinkeSynonyms = Set("die-linke", "linke") map (_ -> "die-linke")
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
