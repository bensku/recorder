package io.github.bensku.recorder.query.cache;

import io.github.bensku.recorder.query.SelectBuilder;

/**
 * A thread-local collection of caches.
 *
 */
public record RecorderCaches(
		QueryCache<SelectBuilder<?>> select
) {}