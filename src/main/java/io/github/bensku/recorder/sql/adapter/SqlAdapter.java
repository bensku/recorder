package io.github.bensku.recorder.sql.adapter;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.stream.Collectors;

import io.github.bensku.recorder.sql.Column;
import io.github.bensku.recorder.sql.JavaType;
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
	
	default String select(Column[] columns, String table, Condition[] conditions) {
		StringBuilder sb = new StringBuilder("SELECT ");
		
		// All columns given (not necessarily all table columns)
		for (int i = 0; i < columns.length - 1; i++) {
			sb.append(columns[i].name()).append(',');
		}
		sb.append(columns[columns.length - 1].name()); // Last without comma at end
		
		// From table with given name
		sb.append(" FROM ").append(table);
		
		if (conditions.length > 0) {
			sb.append(" WHERE ");
			for (int i = 0; i < conditions.length - 1; i++) {
				// TODO
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Maps given type to its SQL counterpart.
	 * @param javaType Java class.
	 * @return String representing an SQL type.
	 */
	String sqlType(JavaType javaType);
	
	/**
	 * Maps given constraint to a string understood by the database.
	 * @param constraint Constraint.
	 * @return String representing the constraint.
	 */
	String constraint(Constraint constraint);
}
