package io.github.bensku.recorder.cache;

/**
 * Interface for Recorder's internal caches.
 *
 * @param <K> Key for cache lookups.
 * @param <V> Value.
 */
public interface Cache<K, V> {

	/**
	 * Gets a value from the cache.
	 * @param key Key for lookup.
	 * @return Value.
	 */
	V get(K key);
}
