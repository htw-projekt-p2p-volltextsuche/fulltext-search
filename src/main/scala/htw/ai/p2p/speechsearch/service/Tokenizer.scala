package htw.ai.p2p.speechsearch.service

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
class Tokenizer {
  private val DelimiterPattern = """[\s,.'"]"""
  private val WrappedWordPattern = """\b(?:\w|\d)+-\r?\n(?:\w|\d)+\b"""
  private val EmptyString = ""

  def tokenize(text: String): List[String] =
    text.toLowerCase
      .replaceAll(WrappedWordPattern, EmptyString)
      .split(DelimiterPattern)
      .filter(!_.isBlank)
      .distinct
      .toList
}
