package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.domain.Searcher._
import htw.ai.p2p.speechsearch.domain.Tokenizer.buildFilterTerm
import htw.ai.p2p.speechsearch.domain.invertedindex.InvertedIndex._
import htw.ai.p2p.speechsearch.domain.model.result.SearchResult._
import htw.ai.p2p.speechsearch.domain.model.result._
import htw.ai.p2p.speechsearch.domain.model.search.Connector._
import htw.ai.p2p.speechsearch.domain.model.search._
import htw.ai.p2p.speechsearch.domain.model.speech._

import scala.annotation.tailrec

/**
 * @author Joscha Seelig <jduesentrieb> 2021
 */
class Searcher(index: Index) {

  val PrioritizedOperators = List(
    AndNot,
    And,
    Or
  )

  def search(search: Search): SearchResult = {
    val ii       = retrieveInvertedIndex(search)
    val queryRes = (Or, processQueryChunk(search.query.terms, ii))
    val additionalRes =
      search.query.additions map (c => (c.connector, processQueryChunk(c.terms, ii)))
    val connected = connectResults(queryRes :: additionalRes)
    val filtered  = applyFilter(search.filter)(connected, ii)

    val results = computeRelevance(filtered, ii)
      .groupMapReduce(_._1)(_._2)(_ + _)
      .toSeq
    val resultEntries = results
      .sortBy(-_._2)
      .take(search.maxResults)
      .map { case (docId, score) => ResultEntry(docId, score) }
      .toList

    SearchResult(results.size, resultEntries)
  }

  private def retrieveInvertedIndex(search: Search): CachedIndex = {
    val queryTokens = (search.query.terms :: search.query.additions.map(_.terms))
      .flatMap(index.tokenizer(_))
      .distinct
    val filterTokens = search.filter
      .map(filter => buildFilterTerm(filter.criteria, filter.value))

    index postings (queryTokens ::: filterTokens)
  }

  private def processQueryChunk(query: String, ii: CachedIndex): PartialResult =
    index
      .tokenizer(query)
      .map(term => ii.getOrElse(term, Nil).groupMap(_.docId)(p => (term, p)))
      .reduce((a, b) => a && b)

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
    filter map (retrieveFilterSet(_, ii)) match {
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
    ii: CachedIndex
  ): List[(DocId, Score)] =
    for {
      (docId, result) <- partial.toList
      (term, posting) <- result
      score            = posting.tf * math.pow(idf(term, ii).getOrElse(0), 2)
    } yield docId -> score / norm(posting)

  private def norm(posting: Posting): Score = math.pow(1 + posting.docLen, 0.5)

  private def idf(term: Term, ii: CachedIndex): Option[Double] = ii get term map idf

  private def idf(docs: PostingList): Double =
    1 + math.log(index.size.toDouble / (docs.size.toDouble + 1))

}

object Searcher {

  type CachedIndex   = Map[Term, PostingList]
  type PartialResult = Map[DocId, List[(Term, Posting)]]

  def apply(index: Index) = new Searcher(index)

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
