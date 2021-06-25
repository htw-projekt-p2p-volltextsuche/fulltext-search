package htw.ai.p2p.speechsearch

import io.circe.generic.extras.Configuration

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object ApplicationConfig {

  implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames.withDefaults

}
