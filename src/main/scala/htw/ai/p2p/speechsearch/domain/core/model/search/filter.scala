package htw.ai.p2p.speechsearch.domain.core.model.search

import enumeratum.values._

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
case class QueryFilter(criteria: FilterCriteria, value: String)

sealed abstract class FilterCriteria(val value: String) extends StringEnumEntry {
  def name: String = value
}

object FilterCriteria
    extends StringEnum[FilterCriteria]
    with StringCirceEnum[FilterCriteria] {
  val values: IndexedSeq[FilterCriteria] = findValues

  case object Affiliation extends FilterCriteria("affiliation")

  case object Speaker extends FilterCriteria("speaker")
}
