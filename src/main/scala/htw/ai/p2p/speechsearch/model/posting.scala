package htw.ai.p2p.speechsearch.model

case class DocId(id: String) extends AnyVal
case class Position(position: Int) extends AnyVal

case class Posting(
    docId: DocId,
    positions: List[Position]
)
