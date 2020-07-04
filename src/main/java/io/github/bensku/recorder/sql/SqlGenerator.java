package io.github.bensku.recorder.sql;

import io.github.bensku.recorder.sql.adapter.SqlAdapter;

@FunctionalInterface
public interface SqlGenerator<T> {

	String generate(SqlAdapter adapter, T query);
}
