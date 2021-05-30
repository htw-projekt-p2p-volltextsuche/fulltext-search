package htw.ai.p2p.speechsearch.domain.model

import java.time.LocalDate

/**
  * @author Joscha Seelig <jduesentrieb> 2021
 **/
case class Speech(
    docId: String,
    title: String,
    speaker: String,
    affiliation: String,
    date: LocalDate,
    text: String
)
