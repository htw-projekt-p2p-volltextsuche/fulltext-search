package htw.ai.p2p.speechsearch.domain.core

import cats.implicits._
import htw.ai.p2p.speechsearch.domain.core.GermanStemmer.{Regions, Suffix, Word}

/**
 * This is an implementation of the snowball stemming for
 * german texts.
 * <br>
 * The algorithm is explained here:
 * https://snowballstem.org/algorithms/german/stemmer.html
 * <br>
 * This code is based on following implementation:
 * https://github.com/stheophil/RelatedTexts/blob/master/src/main/scala/net/theophil/relatedtexts/GermanStemmer.scala
 *
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object GermanStemmer {

  type Word    = String
  type Suffix  = String
  type Regions = (Region, Region)

  private val vowels  = Set('a', 'e', 'i', 'o', 'u', 'y', 'ä', 'ö', 'ü')
  private val sEnding = Set('b', 'd', 'f', 'g', 'h', 'k', 'l', 'm', 'n', 'r', 't')

  private val stEnding = sEnding - 'r'

  def apply(input: Word): Word = (
    setup _
      andThen computeRegions
      andThen (processSteps _ tupled)
      andThen remapChars
  )(input)

  private def processSteps(word: Word, regions: Regions): Word =
    SnowballSteps.foldLeft(word)((w, s) => s.process(w)(regions))

  private def setup(input: Word): Word = {
    val word = input.toLowerCase.replace("ß", "ss")

    val replacements = for {
      i <- 1 until word.length - 1
      c  = word(i)
      if c == 'u' || c == 'y'
      if vowels(word(i - 1)) && vowels(word(i + 1))
    } yield (c.toUpper, i)

    replacements.foldLeft(word) { case (w, (c, i)) =>
      w.updated(i, c)
    }
  }

  private def computeRegions(word: Word): (Word, Regions) = {
    val firstVowel = indexWhere(word, vowels)
    val tempR1 = math.min(
      indexWhere(word, !vowels(_), firstVowel) + 1,
      word.length
    )
    val vowelR1 = indexWhere(word, vowels, tempR1)
    val r2 = math.min(
      indexWhere(word, !vowels(_), vowelR1) + 1,
      word.length
    )
    val r1 = math.max(tempR1, 3)

    (word, (Region(r1), Region(r2)))
  }

  private def indexWhere(
    word: Word,
    condition: Function[Char, Boolean],
    from: Int = 0
  ): Int = {
    val i = word.indexWhere(condition, from)
    if (i == -1) word.length else i
  }

  private def remapChars(word: Word): Word = word map {
    case 'ä' => 'a'
    case 'ö' => 'o'
    case 'ü' => 'u'
    case 'Y' => 'y'
    case 'U' => 'u'
    case c   => c
  }

  private def dropSuffix(word: Word, suffix: Suffix): Word =
    word dropRight suffix.length

  private val R1: ((Region, Region)) => Region = _._1
  private val R2: ((Region, Region)) => Region = _._2
  private def suffixIn(region: Regions => Region) =
    (w: Word, s: Suffix, r: Regions) => region(r) validSuffix (w, s)

  private val dropSuffix: (Word, Suffix, Regions) => Word =
    (w, s, _) => dropSuffix(w, s)

  private val Step1 = SnowballStep(
    Rule(List("em", "ern", "er"), suffixIn(R1), dropSuffix),
    Rule(
      List("e", "en", "es"),
      suffixIn(R1),
      (w, s, _) =>
        dropSuffix(w, s) match {
          case x @ s"${_}niss" => dropSuffix(x, "s")
          case x               => x
        }
    ),
    Rule(
      List("s"),
      (w, s, r) =>
        (r._1 validSuffix (w, s))
          && s.length < w.length
          && sEnding(w(w.length - s.length - 1)),
      dropSuffix
    )
  )

  private val Step2 = SnowballStep(
    Rule(List("en", "er", "est"), suffixIn(R1), dropSuffix),
    Rule(
      List("st"),
      (w, s, r) =>
        (r._1 validSuffix (w, s))
          && s.length + 4 <= w.length
          && stEnding(w(w.length - s.length - 1)),
      dropSuffix
    )
  )

  private val Step3 = SnowballStep(
    Rule(
      List("end", "ung"),
      suffixIn(R2),
      (w, s, r) =>
        dropSuffix(w, s) match {
          case x
              if x.endsWith("ig")
                && !x.endsWith("eig")
                && r._2.validSuffix(x, "ig") =>
            dropSuffix(x, "ig")
          case x => x
        }
    ),
    Rule(
      List("ig", "ik", "isch"),
      (w, s, r) => r._2.validSuffix(w, s) && w(w.length - s.length - 1) != 'e',
      dropSuffix
    ),
    Rule(
      List("lich", "heit"),
      suffixIn(R2),
      (w, s, r) =>
        Rule(List("er", "en"), suffixIn(R1), dropSuffix)(
          dropSuffix(w, s),
          r
        ) merge
    ),
    Rule(
      List("keit"),
      suffixIn(R2),
      (w, s, r) =>
        Rule(List("lich", "ig"), suffixIn(R2), dropSuffix)(
          dropSuffix(w, s),
          r
        ) merge
    )
  )

  private val SnowballSteps = List(Step1, Step2, Step3)

}

case class Region(index: Int) extends AnyVal {

  def validSuffix(word: Word, suffix: Suffix): Boolean =
    index <= word.length - suffix.length

}

case class SnowballStep(rules: Rule*) {

  def process(word: Word)(regions: Regions): Word =
    rules.foldM(word)((word, rule) => rule apply (word, regions)) merge

}

case class Rule(
  suffixes: List[Suffix],
  isValid: (Word, Suffix, Regions) => Boolean,
  modification: (Word, Suffix, Regions) => Word
) {

  def apply(word: Word, regions: Regions): Either[Word, Word] =
    suffixes find word.endsWith match {
      case Some(suffix) =>
        if (isValid(word, suffix, regions))
          modification(word, suffix, regions).asLeft
        else word.asLeft
      case None => word.asRight
    }

}
