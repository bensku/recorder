package io.github.bensku.recorder.sql;

/**
 * A database table that represents a collection of records.
 *
 */
public record Table(
		
		/**
		 * Record that this table represents.
		 */
		JavaType record,
		
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
		 * Can be null if the table doesn't have a primary key.
		 */
		Column primaryKey
) {}