package htw.ai.p2p.speechsearch.domain.model.speech

import htw.ai.p2p.speechsearch.config.CirceConfig._
import io.circe.Codec
import io.circe.generic.extras.semiauto._

import java.time.LocalDate

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
case class Speech(
  docId: DocId,
  title: String,
  speaker: String,
  affiliation: String,
  date: LocalDate,
  text: String
)

object Speech {

  implicit val speechCodec: Codec[Speech] = deriveConfiguredCodec

}
