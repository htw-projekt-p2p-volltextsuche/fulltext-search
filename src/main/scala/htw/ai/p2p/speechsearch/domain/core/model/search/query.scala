package htw.ai.p2p.speechsearch.domain.core.model.search

import enumeratum.EnumEntry.Snakecase
import enumeratum._
import Connector.Or

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
case class Query(
  terms: String,
  additions: List[QueryElement] = Nil
)

case class QueryElement(
  connector: Connector = Or,
  terms: String
)

sealed trait Connector extends EnumEntry with Snakecase

object Connector extends Enum[Connector] with CirceEnum[Connector] {
  val values: IndexedSeq[Connector] = findValues

  case object Or extends Connector

  case object And extends Connector

  case object AndNot extends Connector
}
