package htw.ai.p2p.speechsearch.domain.lru

import cats.Monad
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._

/**
 * Copied from: https://gist.github.com/Daenyth/ee1938ea505210ccd92c7a067c3b0abf
 *
 * @author Daenyth
 */
object LruRef {

  /**
   * Build an immutable LRU key/value store that cannot grow larger than `maxSize`
   */
  def empty[F[_]: Sync, K, V](maxSize: Int): F[LruRef[F, K, V]] =
    Ref[F]
      .of(LruCache[K, V](maxSize))
      .map(new LruRef(_))

}

class LruRef[F[_]: Monad, K, V](lruRef: Ref[F, LruCache[K, V]]) {

  /**
   * Adds the new kv to the LRU
   *
   * @return The kv which was evicted to make space for the new input, if needed
   */
  def put(kv: (K, V)): F[Option[K]] =
    lruRef.modify { lru =>
      (lru + kv).swap
    }

  /** @return The value at `k`, if present */
  def get(k: K): F[Option[V]] = lruRef.modify { lru =>
    lru.get(k).swap
  }

  /** @return The value at `k` prior to removal, if present */
  def remove(k: K): F[Option[V]] = lruRef.modify { lru =>
    lru.remove(k).swap
  }

  /** @return The number of keys present */
  def size: F[Int] = lruRef.get.map(_.size)

  /** @return The set of keys present */
  def keySet: F[Set[K]] = lruRef.get.map(_.keySet)

  def clear: F[Set[K]] = lruRef
    .getAndUpdate(lru => LruCache[K, V](lru.size))
    .map(_.keySet)

}
