package io.github.bensku.recorder.sql;

public record Value(
		String sql
) {
	private static final Value PARAM = new Value("?");
	
	public static Value param() {
		return PARAM;
	}
	
	public static Value column(String name) {
		return new Value(name);
	}
}