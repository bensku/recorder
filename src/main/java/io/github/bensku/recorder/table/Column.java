package io.github.bensku.recorder.table;

/**
 * A column in a table.
 *
 */
public record Column(
		
		/**
		 * Name of this column.
		 */
		String name,
		
		/**
		 * Data type of this column.
		 */
		JavaType type
) {}
