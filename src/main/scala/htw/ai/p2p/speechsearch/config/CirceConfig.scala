package htw.ai.p2p.speechsearch.config

import io.circe.generic.extras.Configuration

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object CirceConfig {

  implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames.withDefaults

}
