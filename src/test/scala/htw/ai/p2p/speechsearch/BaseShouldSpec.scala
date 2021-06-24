package htw.ai.p2p.speechsearch

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
abstract class BaseShouldSpec extends AsyncFlatSpec with AsyncIOSpec with should.Matchers
