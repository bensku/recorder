package io.github.bensku.recorder.table;

import java.lang.reflect.RecordComponent;

import io.github.bensku.recorder.codegen.JavaType;

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
) {
	
	/**
	 * Creates a column from reflection record component.
	 * @param component Core reflection record component.
	 * @return A table column.
	 */
	public static Column fromComponent(RecordComponent component) {
		return new Column(component.getName(), JavaType.fromComponent(component));
	}
}
