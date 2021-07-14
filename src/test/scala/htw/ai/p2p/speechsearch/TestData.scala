package htw.ai.p2p.speechsearch

import cats.effect.IO
import cats.effect.concurrent.Ref
import htw.ai.p2p.speechsearch.SpeechSearchServer.unsafeLogger
import htw.ai.p2p.speechsearch.TestUtils.{
  readLineSetFromFile,
  readSpeechFromFile,
  readSpeechesFromFile
}
import htw.ai.p2p.speechsearch.api.index.IndexService
import htw.ai.p2p.speechsearch.domain.core.invertedindex.InvertedIndex
import htw.ai.p2p.speechsearch.domain.core.invertedindex.InvertedIndex.{
  PostingList,
  Term
}
import htw.ai.p2p.speechsearch.domain.core.model.search.Connector.And
import htw.ai.p2p.speechsearch.domain.core.model.search.FilterCriteria.Speaker
import htw.ai.p2p.speechsearch.domain.core.model.search.{
  Query,
  QueryElement,
  QueryFilter,
  Search
}
import htw.ai.p2p.speechsearch.domain.core.model.speech.Speech
import htw.ai.p2p.speechsearch.domain.core.{Indexer, Tokenizer}

import java.util.UUID

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
object TestData {

  val ValidUuid1: UUID = UUID.fromString("b272829e-f15b-44fe-8c25-25e3eff45300")
  val ValidUuid2: UUID = UUID.fromString("32271a61-5c42-452f-8d59-7a2323c88bff")
  val ValidUuid3: UUID = UUID.fromString("dbcb75a0-477c-4bf2-9de3-9487bb164678")

  val EntireSearch: Search = Search(
    query = Query(
      terms = "hello",
      additions = List(QueryElement(And, "world"))
    ),
    filter = List(
      QueryFilter(criteria = Speaker, value = "Nelson Mandela")
    )
  )

  val TestTokenizer: Tokenizer = Tokenizer(
    readLineSetFromFile("stopwords_de.txt")
  )

  val TestInvertedIndex: IO[InvertedIndex[IO]] =
    for {
      indexRef <- Ref[IO].of(Map.empty[Term, PostingList])
    } yield InvertedIndex.local[IO](indexRef)

  def seededIndex(speeches: Speech*): IO[InvertedIndex[IO]] =
    for {
      ii     <- TestInvertedIndex
      indexer = Indexer(TestTokenizer)
      service = IndexService.impl[IO](indexer, ii)
      _      <- service insert List(speeches: _*)
    } yield ii

  def seededIndex: IO[InvertedIndex[IO]] = seededIndex(sampleSpeeches: _*)

  def sampleSpeeches: List[Speech] = readSpeechesFromFile("sample_speeches.json")

}
