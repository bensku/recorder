package io.github.bensku.recorder.record;

import java.util.Objects;

public class PrimaryKey<T> {

	public static <T> PrimaryKey<T> auto() {
		return new PrimaryKey<>(null);
	}
	
	public static <T> PrimaryKey<T> of(T value) {
		// null is marker for not-yet present keys, don't allow it to be set
		Objects.requireNonNull(value, "primary key cannot be null");
		return new PrimaryKey<>(value);
	}
	
	private T value;
	
	/**
	 * @deprecated For internal use only.
	 */
	@Deprecated
	public PrimaryKey(T value) {
		this.value = value;
	}
	
	public boolean isPresent() {
		return value != null;
	}
	
	private void guardPresent() {
		if (!isPresent()) {
			throw new IllegalStateException("primary key not yet present");
		}
	}
	
	public T value() {
		guardPresent();
		return value;
	}
	
	// TODO foreign key creation, how can we identify owner of primary key?
}
