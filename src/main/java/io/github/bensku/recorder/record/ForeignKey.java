package io.github.bensku.recorder.record;

public class ForeignKey<T> {

	/**
	 * Value of the column.
	 */
	private final Object value;
	
	/**
	 * Record of the table that this foreign key targets.
	 */
	private final Class<? extends Record> target;
	
	/**
	 * @deprecated For internal use only.
	 */
	@Deprecated
	public ForeignKey(Object value, Class<? extends Record> target) {
		this.value = value;
		this.target = target;
	}
	
	public Object value() {
		return value;
	}
	
	public Class<? extends Record> target() {
		return target;
	}
}
