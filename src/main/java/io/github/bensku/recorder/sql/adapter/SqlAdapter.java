package io.github.bensku.recorder.sql.adapter;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.github.bensku.recorder.sql.Column;
import io.github.bensku.recorder.sql.Table;
import io.github.bensku.recorder.sql.constraint.Constraint;

public interface SqlAdapter {

	/**
	 * Generates a statement to create given table.
	 * @param table Table definition.
	 * @return CREATE TABLE statement.
	 */
	default String createTable(Table table) {
		StringBuilder sb = new StringBuilder("CREATE TABLE ");
		sb.append(table.name()).append(" (");
		
		// Column definitions
		Column[] columns = table.columns();
		for (int i = 0; i < columns.length - 1; i++) {
			sb.append(column(columns[i])).append(',');
		}
		sb.append(column(columns[columns.length - 1])); // Last without comma at end
		
		sb.append(")"); // End create table
		
		return sb.toString();
	}
	
	/**
	 * Generates SQL for a column.
	 * @param column Column definition.
	 * @return Column SQL string.
	 */
	default String column(Column column) {
		return column.name() + sqlType(column.type()) + columnConstraints(column.constraints());
	}
	
	/**
	 * Generates SQL for column constraints.
	 * @param constraints Array of constraints.
	 * @return Constraints as string.
	 */
	default String columnConstraints(Constraint[] constraints) {
		return Arrays.stream(constraints).map(this::constraint).collect(Collectors.joining(" "));
	}
	
	/**
	 * Maps given type to its SQL counterpart.
	 * @param javaType Java class.
	 * @return String representing an SQL type.
	 */
	String sqlType(Class<?> javaType);
	
	/**
	 * Maps given constraint to a string understood by the database.
	 * @param constraint Constraint.
	 * @return String representing the constraint.
	 */
	String constraint(Constraint constraint);
}
