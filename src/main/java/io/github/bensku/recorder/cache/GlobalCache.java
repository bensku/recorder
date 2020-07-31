package io.github.bensku.recorder.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Thread-safe cache that can create thread-local views to it.
 * Note that values are never evicted from this cache, making it
 * unsuitable for e.g. queries (use {@link QueryCache} instead.
 */
public class GlobalCache<K, V> implements Cache<K, V> {
	
	/**
	 * Loads values that are not in this cache.
	 */
	private final Function<K, V> loader;

	/**
	 * Underlying concurrent map of this cache.
	 */
	private final ConcurrentMap<K, V> map;
	
	/**
	 * Thread local caches.
	 */
	private final ThreadLocal<LocalCache<K, V>> localCache;
	
	public GlobalCache(Function<K, V> loader) {
		this.loader = loader;
		this.map = new ConcurrentHashMap<>();
		GlobalCache<K, V> self = this;
		this.localCache = new ThreadLocal<>() {
			// When a thread asks for a local cache, ensure one is available
			protected LocalCache<K, V> initialValue() {
				return new LocalCache<>(self);
			}
		};
	}

	@Override
	public V get(K key) {
		// Get from cache or create new value
		return map.computeIfAbsent(key, loader::apply);
	}
	
	/**
	 * Gets a thread-local view into this cache. The view has its own cache to
	 * reduce contention on the global cache.
	 * @return Cache view.
	 */
	public Cache<K, V> threadLocalView() {
		return localCache.get();
	}
	
}
