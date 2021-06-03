package htw.ai.p2p.speechsearch.api.indexes

import htw.ai.p2p.speechsearch.domain.model.{DocId, Speech}

import java.time.LocalDate

/**
  * @author Joscha Seelig <jduesentrieb> 2021
**/
final case class SpeechData(
    docId: String,
    title: String,
    speaker: String,
    affiliation: String,
    date: String,
    text: String
) {
  def toDomain: Speech =
    Speech(
      docId = DocId(docId),
      title = title,
      speaker = speaker,
      affiliation = affiliation,
      date = LocalDate.parse(date),
      text = text
    )
}
