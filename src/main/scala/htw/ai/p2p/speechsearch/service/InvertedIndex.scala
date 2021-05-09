package htw.ai.p2p.speechsearch.service

import htw.ai.p2p.speechsearch.model.Posting

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
  * one for supplying and retrieving batches of term-postings
  * pairs. The batch methods should be utilized to improve
  * the performance if possible.
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
  def apply(term: Term): Option[PostingList] = get(term)

  /**
    * Alias for `insert`.
    */
  def :+(entry: (Term, PostingList)): InvertedIndex =
    entry match {
      case (term, postings) => insert(term, postings)
    }

  /**
    * Alias for `insertAll`
    */
  def :++(entries: Map[Term, PostingList]): InvertedIndex =
    insertAll(entries)

  /**
    * Stores a `List` of postings in a new `InvertedIndex`
    * by mapping them to the specified term as key.
    * The given postings will be appended to the present
    * postings if the given term is already present.
    *
    * @param term The term too which the postings will
    *             be appended to.
    * @param postings The postings that are to be stored
    *                 in the inverted index.
    *
    * @return A new inverted index updated with the given
    *         postings.
    */
  def insert(term: Term, postings: PostingList): InvertedIndex

  /**
    * Stores all specified postings to the mapped term in
    * one batch in a new `InvertedIndex`.
    * Just as for `insert` the postings will be appended
    * to an already present `List` of postings if the term
    * is already known.
    *
    * This method should if possible improve the performance
    * by utilizing the fact that this method handles a batch
    * of items.
    *
    * @param entries A `Map` containing the postings mapped
    *                to the keys at which they are to be
    *                stored.
    * @return A new inverted index updated with the given
    *         postings.
    */
  def insertAll(entries: Map[Term, PostingList]): InvertedIndex

  /**
    * Returns an `Option` containing a `List` of postings that
    * are mapped to the specified term or an empty `Option`
    * if the key is not present.
    *
    * @param term The term for which the postings are to
    *             be retrieved.
    *
    * @return An `Option` containing the requested postings
    *         or an empty one if the term is not present.
    */
  def get(term: Term): Option[PostingList]

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
