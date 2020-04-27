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
public class StatementCache<T> {
	
	private class NewGenEntry {
		public final String sql;
		public int usedCount;
		
		public NewGenEntry(String sql) {
			this.sql = sql;
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
	private final Map<T, String> oldGen;
	
	public StatementCache(int newGenSize, int promoteThreshold) {
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
	 * @return SQL string or null if it is not in this cache.
	 */
	public String get(T key) {
		String sql = oldGen.get(key);
		if (sql != null) { // Found from old gen
			return sql;
		}
		NewGenEntry entry = newGen.get(key);
		if (entry != null) { // Found from new gen
			entry.usedCount++;
			if (entry.usedCount == promoteTreshold) { // Promote to old gen if it has been used enough
				newGen.remove(entry);
				oldGen.put(key, sql);
			}
			return entry.sql;
		}
		
		return null; // Not found at all
	}
	
	/**
	 * Puts an SQL string to this cache.
	 * @param key Key for it.
	 * @param sql SQL string to cache.
	 */
	public void put(T key, String sql) {
		newGen.put(key, new NewGenEntry(sql)); // Put to new gen
	}
}
