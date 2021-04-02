package io.github.bensku.recorder.table;

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
