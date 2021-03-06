package io.github.bensku.recorder.sql.constraint;

import io.github.bensku.recorder.record.Foreign;
import io.github.bensku.recorder.record.Nullable;
import io.github.bensku.recorder.record.PrimaryKey;
import io.github.bensku.recorder.record.Unique;

/**
 * Column-specific constraint.
 *
 */
public enum SimpleConstraint implements Constraint {
	
	/**
	 * Column must not be null. This is added by default, but can be omitted
	 * with <i>any</i> annotation named "Nullable". We ship {@link Nullable}
	 * for those who do not have it from any other library.
	 */
	NOT_NULL,
	
	/**
	 * Values of this column in all rows must be unique.
	 * Added with {@link Unique} annotation.
	 */
	UNIQUE,
	
	/**
	 * This column is the primary key of the table. {@link Foreign} keys will
	 * refer to this. Added with {@link PrimaryKey} annotation.
	 * On most databases, column being primary key implies that it is also
	 * unique and not nullable.
	 */
	PRIMARY_KEY,
	
	/**
	 * Values of this column are generated by database when inserting rows.
	 * User-specified values are ignored.
	 * Added by default with {@link #PRIMARY_KEY} and int or long, but can
	 * be disabled by setting {@link PrimaryKey#generated()} to false.
	 */
	GENERATED
}
