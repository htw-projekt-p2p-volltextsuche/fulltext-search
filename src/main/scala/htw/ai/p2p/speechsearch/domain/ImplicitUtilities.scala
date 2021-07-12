package htw.ai.p2p.speechsearch.domain

import mojolly.inflector.InflectorImports._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object ImplicitUtilities {

  implicit class FormalizedString(self: String) {

    def formalize(amount: Int): String =
      if (amount == 1) self.singularize else self.pluralize

  }

}
