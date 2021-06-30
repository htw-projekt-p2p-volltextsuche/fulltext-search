package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestData.preparedTokenizer
import htw.ai.p2p.speechsearch.domain.Tokenizer.buildFilterTerm
import htw.ai.p2p.speechsearch.domain.model.search.FilterCriteria.Affiliation
import org.scalatest.prop.TableDrivenPropertyChecks._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class TokenizerSpec extends BaseShouldSpec {

  private val Tokenizer = preparedTokenizer

  private val SampleText = "ERP steht für Enterprise-Resource-Planning oder " +
    "Unternehmens-Informationssystem, womit alle geschäftsrelevanten " +
    "Bereiche eines Unternehmens im Zusammenhang betrachtet werden können."

  "A Tokenizer" should "tokenize words with hyphens as one token" in {
    val tokens = Tokenizer apply SampleText

    tokens should contain allOf (
      GermanStemmer("enterprise-resource-planning"),
      GermanStemmer("unternehmens-informationssystem")
    )
  }

  it should "remove stop words when tokenizing" in {
    val tokens = Tokenizer apply SampleText

    tokens should have size 10
  }

  private val normalizedAffiliations = Table(
    ("raw", "normalized"),
    ("Die Grünen", "die-grünen"),
    ("Bündnis 90 / Die Grünen", "die-grünen"),
    ("Grüne", "die-grünen"),
    ("CDU", "cdu-csu"),
    ("CSU", "cdu-csu"),
    ("CDU / CSU", "cdu-csu"),
    ("CDU -- CSU", "cdu-csu"),
    ("Linke", "die-linke"),
    ("Die Linke", "die-linke"),
    ("spd", "spd")
  )

  it should "normalize affiliation filter terms" in {
    forAll(normalizedAffiliations) { (raw: String, normalized: String) =>
      val filterTerm = buildFilterTerm(Affiliation, raw)

      filterTerm should endWith(normalized)
    }
  }

}
