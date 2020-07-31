package io.github.bensku.recorder.sql;

import io.github.bensku.recorder.cache.CachedQuery;
import io.github.bensku.recorder.sql.adapter.SqlAdapter;

@FunctionalInterface
public interface QueryGenerator<T> {

	CachedQuery generate(SqlAdapter adapter, T query);
}
