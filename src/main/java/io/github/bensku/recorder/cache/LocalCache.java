package io.github.bensku.recorder.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-local cache that asks keys it does not yet have from a global cache.
 */
public class LocalCache<K, V> implements Cache<K, V> {

	/**
	 * Global cache to delegate to.
	 */
	private final Cache<K, V> parent;
	
	/**
	 * Thread-local map.
	 */
	private final Map<K, V> map;
	
	public LocalCache(Cache<K, V> parent) {
		this.parent = parent;
		this.map = new HashMap<>();
	}

	@Override
	public V get(K key) {
		// Get from here or from parent
		return map.computeIfAbsent(key, parent::get);
	}

}
