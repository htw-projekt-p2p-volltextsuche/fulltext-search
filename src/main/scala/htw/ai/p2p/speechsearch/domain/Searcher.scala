package htw.ai.p2p.speechsearch.domain

import cats.implicits._
import htw.ai.p2p.speechsearch.domain.Searcher._
import htw.ai.p2p.speechsearch.domain.Tokenizer.buildFilterTerm
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex._
import htw.ai.p2p.speechsearch.domain.model.result.SearchResult._
import htw.ai.p2p.speechsearch.domain.model.result.{ResultEntry, SearchResult}
import htw.ai.p2p.speechsearch.domain.model.search.Connector._
import htw.ai.p2p.speechsearch.domain.model.search._
import htw.ai.p2p.speechsearch.domain.model.speech._

import scala.annotation.tailrec

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class Searcher(val tokenizer: Tokenizer) {

  val PrioritizedOperators = List(AndNot, And, Or)

  def search(search: Search, ii: CachedIndex, indexSize: Int): SearchResult = {
    val queryRes = (Or, processQueryChunk(search.query.terms, ii, tokenizer))
    val additionalRes = search.query.additions map (c =>
      c.connector -> processQueryChunk(c.terms, ii, tokenizer)
    )
    val connected = connectResults(queryRes :: additionalRes)
    val filtered  = applyFilter(search.filter)(connected, ii)

    val results = computeRelevance(filtered, ii, indexSize)
      .groupMapReduce(_._1)(_._2)(_ + _)
      .toSeq
    val resultEntries = results
      .sortBy(-_._2)
      .take(search.maxResults)
      .map { case (docId, score) => ResultEntry(docId, score) }
      .toList

    SearchResult(results.size, resultEntries)
  }

  private def processQueryChunk(
    query: String,
    ii: CachedIndex,
    tokenizer: Tokenizer
  ): PartialResult =
    tokenizer(query)
      .map(term => ii.getOrElse(term, Nil).groupMap(_.docId)(p => (term, p)))
      .reduceOption((a, b) => a && b)
      .getOrElse(Map.empty)

  private def connectResults(
    chunkResults: List[(Connector, PartialResult)]
  ): PartialResult = {
    @tailrec
    def go(
      rem: List[(Connector, PartialResult)],
      Connector: Connector,
      res: List[(Connector, PartialResult)] = Nil
    ): List[(Connector, PartialResult)] =
      rem match {
        case (ca, a: PartialResult) :: (Connector, b: PartialResult) :: xs =>
          val (matching, rest) = xs.span(_._1 == Connector)
          val connected        = connect(Connector)(a :: b :: matching.map(_._2))
          go(rest, Connector, res :+ (ca, connected))
        case Nil      => res
        case x :: Nil => res :+ x
        case x :: xs  => go(xs, Connector, res :+ x)
      }

    PrioritizedOperators
      .foldLeft(chunkResults)(go(_, _))
      .flatMap(_._2)
      .toMap
  }

  private def connect(
    connector: Connector
  )(chunks: List[PartialResult]): PartialResult =
    chunks reduce ((a, b) =>
      connector match {
        case Or     => a ++ b
        case And    => a && b
        case AndNot => a -- b.keySet
      }
    )

  private def applyFilter(
    filter: List[QueryFilter]
  )(chunkResult: PartialResult, ii: CachedIndex): PartialResult =
    filter
      .filterNot(_.value.isBlank)
      .groupMapReduce(_.criteria)(retrieveFilterSet(_, ii))((a, b) =>
        connect(Or)(List(a, b))
      )
      .values
      .toList match {
      case Nil        => chunkResult
      case filterSets => connect(And)(chunkResult :: filterSets)
    }

  private def retrieveFilterSet(
    filter: QueryFilter,
    ii: CachedIndex
  ): PartialResult = {
    val term = buildFilterTerm(filter.criteria, filter.value)
    ii.getOrElse(term, Nil).groupMap(_.docId)(p => (term, p))
  }

  private def computeRelevance(
    partial: PartialResult,
    ii: CachedIndex,
    indexSize: Int
  ): List[(DocId, Score)] =
    for {
      (docId, result) <- partial.toList
      (term, posting) <- result
      score            = posting.tf * math.pow(idf(term, ii, indexSize).getOrElse(0), 2)
    } yield docId -> score / norm(posting)

  private def norm(posting: Posting): Score = math.pow(1 + posting.docLen, 0.5)

  private def idf(term: Term, ii: CachedIndex, indexSize: Int): Option[Double] =
    ii get term map (idf(_, indexSize))

  private def idf(docs: PostingList, indexSize: Int): Double =
    1 + math.log(indexSize / (docs.size + 1).toDouble)

}

object Searcher {

  type CachedIndex   = Map[Term, PostingList]
  type PartialResult = Map[DocId, List[(Term, Posting)]]

  def apply(tokenizer: Tokenizer) = new Searcher(tokenizer)

  implicit class ConnectableResult(p: PartialResult) {

    /**
     * Intersects this partial result to the specified one by key.
     *
     * @param o The partial result that is to be intersected.
     * @return A new partial result representing the intersection of the given partial results.
     */
    def &&(o: PartialResult): PartialResult =
      (p.keySet & o.keySet)
        .map(key => (key, p(key) ::: o(key)))
        .toMap

  }

}
