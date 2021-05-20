package htw.ai.p2p.speechsearch.model

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
case class Speech(
    docId: DocId,
    content: String,
    speaker: String,
    affiliation: String
)
