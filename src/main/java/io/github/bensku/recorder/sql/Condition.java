package io.github.bensku.recorder.sql;

public record Condition(
		Value lhs,
		Type type,
		Value rhs
) {
	
	public enum Type {
		EQUAL
	}

}
