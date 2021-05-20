package htw.ai.p2p.speechsearch.service

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

    val tokens = tokenizer tokenize text

    tokens should have size 17
    tokens should contain allOf (
      "enterprise-resource-planning",
      "unternehmens-informationssystem"
    )
  }

  ignore should "remove hyphen with linebreak from a wrapped word" in {
    val text = """This text con-
        |tains a wrapped word.""".stripMargin

    val tokens = tokenizer tokenize text

    tokens should have size 6
    tokens should contain("contains")
  }

}
