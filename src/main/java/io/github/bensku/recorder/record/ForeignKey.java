package io.github.bensku.recorder.record;

public class ForeignKey<T> {

	private final T value;
	
	ForeignKey(T value) {
		this.value = value;
	}
	
	public T value() {
		return value;
	}
}
