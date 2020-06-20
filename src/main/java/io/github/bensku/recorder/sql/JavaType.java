package io.github.bensku.recorder.sql;

import io.github.bensku.recorder.record.Foreign;

public record JavaType(
		String name
) {
	
	public static JavaType of(Class<?> c) {
		return new JavaType(c.getName());
	}
	
	public static final JavaType BOOLEAN = new JavaType("boolean");
	public static final JavaType BYTE = new JavaType("byte");
	public static final JavaType SHORT = new JavaType("short");
	public static final JavaType INT = new JavaType("int");
	public static final JavaType LONG = new JavaType("long");
	public static final JavaType FLOAT = new JavaType("float");
	public static final JavaType DOUBLE = new JavaType("double");
	public static final JavaType FOREIGN = of(Foreign.class);
	
	public String internalName() {
		return name.replace('.', '/');
	}
}
