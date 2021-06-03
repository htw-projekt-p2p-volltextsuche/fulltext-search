package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.BaseShouldSpec

/**
  *@author Joscha Seelig <jduesentrieb> 2021
**/
class TokenizerSpec extends BaseShouldSpec {

  val tokenizer = new Tokenizer()

  "A Tokenizer" should "tokenize words with hyphens as one token" in {
    val text = "ERP steht für Enterprise-Resource-Planning oder " +
      "Unternehmens-Informationssystem, womit alle geschäftsrelevanten " +
      "Bereiche eines Unternehmens im Zusammenhang betrachtet werden können."

    val tokens = tokenizer apply text

    tokens should have size 11
    tokens should contain allOf (
      "enterprise-resource-planning",
      "unternehmens-informationssystem"
    )
  }

}
