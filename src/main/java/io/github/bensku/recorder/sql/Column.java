package io.github.bensku.recorder.sql;

import io.github.bensku.recorder.sql.constraint.Constraint;

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
		JavaType type,
		
		/**
		 * Constraints specific to this column.
		 */
		Constraint[] constraints
) {}
