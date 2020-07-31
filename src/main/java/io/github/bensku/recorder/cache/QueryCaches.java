package io.github.bensku.recorder.cache;

import io.github.bensku.recorder.query.SelectBuilder;

/**
 * A thread-local collection of caches.
 *
 */
public record QueryCaches(
		QueryCache<SelectBuilder<?>> select
) {
	
	public QueryCaches(int newGenSize, int promoteThreshold) {
		this(new QueryCache<>(newGenSize, promoteThreshold));
	}
}