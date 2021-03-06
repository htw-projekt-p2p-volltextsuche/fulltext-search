package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestData.TestTokenizer
import htw.ai.p2p.speechsearch.TestUtils.TestString
import htw.ai.p2p.speechsearch.domain.core.Tokenizer.buildFilterTerm
import htw.ai.p2p.speechsearch.domain.core.model.search.FilterCriteria.Affiliation
import org.scalatest.prop.TableDrivenPropertyChecks._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class TokenizerSpec extends BaseShouldSpec {

  private val Tokenizer = TestTokenizer

  private val SampleText = "ERP steht für Enterprise-Resource-Planning oder " +
    "Unternehmens-Informationssystem, womit alle geschäftsrelevanten " +
    "Bereiche eines Unternehmens im Zusammenhang betrachtet werden können."

  "A Tokenizer" should "tokenize words with hyphens as one token" in {
    val tokens = Tokenizer apply SampleText

    tokens should contain allOf (
      "enterprise-resource-planning".stemmed,
      "unternehmens-informationssystem".stemmed
    )
  }

  it should "interpret words separated by non breaking spaces as one term" in {
    val tokens =
      Tokenizer apply "NonBreaking\u00A0Space NonBreaking\u00A0-\u00A0Space"

    tokens should have size 2
  }

  it should "remove stop words when tokenizing" in {
    val tokens = Tokenizer apply SampleText

    tokens should have size 10
  }

  private val normalizedAffiliations = Table(
    ("raw", "normalized"),
    ("Die Grünen", "bündnis-90-die-grünen"),
    ("Bündnis 90 / Die Grünen", "bündnis-90-die-grünen"),
    ("Grüne", "bündnis-90-die-grünen"),
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
