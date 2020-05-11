package io.github.bensku.recorder.sql;

/**
 * A database table that represents a collection of records.
 *
 */
public record Table(
		
		/**
		 * Name of the table.
		 */
		String name,
		
		/**
		 * Table columns.
		 */
		Column[] columns,
		
		/**
		 * Primary key column. It is also contained in {@link #columns()}.
		 */
		Column primaryKey
) {}