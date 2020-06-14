package io.github.bensku.recorder.sql;

public record JavaType(
		String name
) {
	
	public static final JavaType BOOLEAN = new JavaType("boolean");
	public static final JavaType BYTE = new JavaType("byte");
	public static final JavaType SHORT = new JavaType("short");
	public static final JavaType INT = new JavaType("int");
	public static final JavaType LONG = new JavaType("long");
	public static final JavaType FLOAT = new JavaType("float");
	public static final JavaType DOUBLE = new JavaType("double");
	
	public String internalName() {
		return name.replace('.', '/');
	}
}
