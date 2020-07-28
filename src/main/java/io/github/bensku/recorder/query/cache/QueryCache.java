package io.github.bensku.recorder.query.cache;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.bensku.recorder.query.SelectBuilder;

/**
 * A single-threaded, two-stage (new/old generation) cache for
 * SQL strings of statements.
 * @param <T> Type of keys used for lookups.
 */
public class QueryCache<T> {
	
	private class NewGenEntry {
		public final CachedQuery query;
		public int usedCount;
		
		public NewGenEntry(CachedQuery query) {
			this.query = query;
			this.usedCount = 0;
		}
	}

	/**
	 * The new generation cache receives entries that cannot be found from the
	 * caches. Entries are promoted to the old generation cache once they reach
	 * sufficient {@link NewGenEntry#usedCount}. When this cache gets full,
	 * oldest entries that have not been promoted are removed.
	 */
	private final LinkedHashMap<T, NewGenEntry> newGen;
	
	/**
	 * New generation cache promotion threshold.
	 */
	private final int promoteTreshold;
	
	/**
	 * The old generation cache contains statements that have been promoted
	 * from the new generation cache. Statements are never dropped from it.
	 */
	private final Map<T, CachedQuery> oldGen;
	
	public QueryCache(int newGenSize, int promoteThreshold) {
		this.newGen = new LinkedHashMap<>(newGenSize) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<T, NewGenEntry> eldest) {
				return size() == newGenSize;
			}
		};
		this.promoteTreshold = promoteThreshold;
		this.oldGen = new HashMap<>();
	}
	
	/**
	 * Attempts to get SQL corresponding to the given key.
	 * @param key Key, e.g. {@link SelectBuilder}.
	 * @return Cached query data or null if it is not in this cache.
	 */
	public CachedQuery get(T key) {
		CachedQuery query = oldGen.get(key);
		if (query != null) { // Found from old gen
			return query;
		}
		NewGenEntry entry = newGen.get(key);
		if (entry != null) { // Found from new gen
			entry.usedCount++;
			if (entry.usedCount == promoteTreshold) { // Promote to old gen if it has been used enough
				newGen.remove(entry);
				oldGen.put(key, query);
			}
			return entry.query;
		}
		
		return null; // Not found at all
	}
	
	/**
	 * Puts an SQL string to this cache.
	 * @param key Key for it.
	 * @param query Cached query data.
	 */
	public void put(T key, CachedQuery query) {
		newGen.put(key, new NewGenEntry(query)); // Put to new gen
	}
}
