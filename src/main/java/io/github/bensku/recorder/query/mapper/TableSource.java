package io.github.bensku.recorder.query.mapper;

import io.github.bensku.recorder.sql.JavaType;
import io.github.bensku.recorder.sql.Table;

/**
 * Provides tables for {@link JavaType Java types}.
 *
 */
@FunctionalInterface
public interface TableSource {

	/**
	 * Gets a table definition for given type.
	 * @param type Java type.
	 * @return Table definition.
	 */
	Table get(JavaType type);
}
