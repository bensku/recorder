package io.github.bensku.recorder.query.cache;

/**
 * Cached SQL query.
 *
 */
public record CachedQuery(
		
		/**
		 * SQL query string, with placeholders for parameters.
		 */
		String sql,
		
		/**
		 * Parameter sources for placeholders, in order they appear in SQL.
		 * Usage of these numbers depends on type of the query. They're
		 * usually some kind of indices to arrays.
		 */
		int[] parameterSources
) {}
