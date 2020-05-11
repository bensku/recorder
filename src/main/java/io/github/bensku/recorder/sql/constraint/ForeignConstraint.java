package io.github.bensku.recorder.sql.constraint;

import io.github.bensku.recorder.record.Foreign;
import io.github.bensku.recorder.sql.Table;

/**
 * Column refers to a row in another table by its primary key.
 * Record components of type {@link Foreign} have this.
 *
 */
public record ForeignConstraint(
		
		/**
		 * The table this foreign key refers to.
		 */
		Table foreignTable
) implements Constraint {}
