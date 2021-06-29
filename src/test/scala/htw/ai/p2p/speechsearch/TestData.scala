package htw.ai.p2p.speechsearch

import htw.ai.p2p.speechsearch.TestUtils.readLineSetFromFile
import htw.ai.p2p.speechsearch.domain.invertedindex.LocalInvertedIndex
import htw.ai.p2p.speechsearch.domain.model.search.Connector.And
import htw.ai.p2p.speechsearch.domain.model.search.FilterCriteria.Speaker
import htw.ai.p2p.speechsearch.domain.model.search._
import htw.ai.p2p.speechsearch.domain.{Index, Tokenizer}

import java.util.UUID

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object TestData {

  val ValidUuid1: UUID = UUID.fromString("b272829e-f15b-44fe-8c25-25e3eff45300")
  val ValidUuid2: UUID = UUID.fromString("32271a61-5c42-452f-8d59-7a2323c88bff")
  val ValidUuid3: UUID = UUID.fromString("dbcb75a0-477c-4bf2-9de3-9487bb164678")

  val EntireSearch: Search = Search(
    maxResults = 15,
    query = Query(
      terms = "hello",
      additions = List(QueryElement(And, "world"))
    ),
    filter = List(
      QueryFilter(criteria = Speaker, value = "Nelson Mandela")
    )
  )

  val preparedTokenizer: Tokenizer = Tokenizer(
    readLineSetFromFile("stopwords_de.txt")
  )

  val preparedIndex: Index = Index(
    preparedTokenizer,
    LocalInvertedIndex()
  )

}
