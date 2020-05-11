package io.github.bensku.recorder.record;

/**
 * Refers a to a record in foreign table by their
 * {@link PrimaryKey primary key}.
 *
 * @param <T> Table type.
 */
public interface Foreign<T> {
	
	/**
	 * Queries this foreign record.
	 * @return Foreign record.
	 */
	T get();
	
}
