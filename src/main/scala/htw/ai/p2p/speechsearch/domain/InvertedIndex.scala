package htw.ai.p2p.speechsearch.domain

import htw.ai.p2p.speechsearch.domain.model.Posting

/**
  * Interface for the encapsulation of an inverted index.
  * The intention of this interface is to decouple the main
  * search logic from the implementation of the inverted
  * index.
  * From an outer point of view it doesn't matter, if the
  * inverted index is realized locally or if it is distributed
  * over the network for example.
  *
  * The inverted index maps single terms to a list of
  * associated postings.
  *
  * There are two types of methods defined for accessing the
  * inverted index. One for the addition and removal
  * of single terms with their associated postings and another
  * one for supplying and retrieving batches of term-posting
  * pairs. The batch methods should be utilized to improve
  * the performance if possible.
  *
  * @author Joscha Seelig <jduesentrieb> 2021
  */
trait InvertedIndex {

  /**
    * Representation of a list of postings.
    */
  type PostingList = List[Posting]

  /**
    * Representation of a single search term
    */
  type Term = String

  /**
    * Alias for ´get´.
    */
  def apply(term: Term): PostingList = get(term)

  /**
    * Alias for `insert`.
    */
  def :+(entry: (Term, Posting)): InvertedIndex =
    insert(entry._1, entry._2)

  /**
    * Alias for `insertAll`
    */
  def :++(entries: Map[Term, Posting]): InvertedIndex =
    insertAll(entries)

  /**
    * Returns the amount of actually indexed documents.
    */
  def size: Int

  /**
    * Adds a Posting into a new `InvertedIndex`
    * by mapping it to the specified term as key.
    * The given posting will be appended to the available
    * postings if the given term is already present.
    *
    * @param term The term to which the posting will
    *             be appended to.
    * @param posting The posting that is to be stored
    *                 in the inverted index.
    *
    * @return A new inverted index updated with the given
    *         term-posting-pair.
    */
  def insert(term: Term, posting: Posting): InvertedIndex

  /**
    * Adds all specified postings to the associated term
    * in one batch into a new `InvertedIndex`.
    * Just as for `insert` each posting will be appended
    * to an already present `List` of postings if the term
    * is already known.
    *
    * This method should if possible improve the performance
    * by utilizing the fact that this method handles a batch
    * of items.
    *
    * @param entries A `Map` containing postings mapped
    *                to a term to which they are associated.
    *
    * @return A new inverted index updated with the given
    *         postings.
    */
  def insertAll(entries: Map[Term, Posting]): InvertedIndex

  /**
    * Returns a `List` containing the postings associated
    * to the specified term or an empty `List` if the key
    * is not present.
    *
    * @param term The term for which the postings are to
    *             be retrieved.
    *
    * @return A `List` containing the requested postings
    *         or an empty one if the term is not present.
    */
  def get(term: Term): PostingList

  /**
    * Retrieves all postings that are stored at the given
    * terms and returns a `Map` containing all known terms
    * with the associated postings.
    * All terms that could not be found will not be part of
    * the result map, so that an empty `Map` is returned if
    * none of the given terms are known.
    *
    * @param terms A `List` of terms whose associated postings
    *              are to be retrieved.
    *
    * @return A `Map` containing all the terms that could be
    *         found mapped to the associated postings.
    */
  def getAll(terms: List[Term]): Map[Term, PostingList]
}
