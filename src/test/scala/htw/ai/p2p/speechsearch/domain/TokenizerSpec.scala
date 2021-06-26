package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.BaseShouldSpec
import htw.ai.p2p.speechsearch.TestData.preparedTokenizer
import htw.ai.p2p.speechsearch.domain.Tokenizer.buildFilterTerm
import htw.ai.p2p.speechsearch.domain.model.search.FilterCriteria.Affiliation

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

    tokens should contain allOf(
      "enterprise-resource-planning",
      "unternehmens-informationssystem"
    )
  }

  it should "remove stop words when tokenizing" in {
    val tokens = Tokenizer apply SampleText

    tokens should have size 11
  }

  it should "normalize affiliation filter terms" in {
    val affiliation = "Bündnis 90  die Grünen"

    val filterTerm = buildFilterTerm(Affiliation, affiliation)

    filterTerm endsWith ":die-grünen" shouldBe true
  }

}
