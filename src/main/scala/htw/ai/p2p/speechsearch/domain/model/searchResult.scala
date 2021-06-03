package htw.ai.p2p.speechsearch.domain.model

case class SearchResult(results: Seq[ResultEntry])

case class ResultEntry(docId: DocId, score: Double)
