package io.github.bensku.recorder.sql;

import org.objectweb.asm.Type;

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
	public static final JavaType STRING = of(String.class);
	public static final JavaType FOREIGN = of(Foreign.class);
	
	public String internalName() {
		return name.replace('.', '/');
	}
	
	public Type toAsmType() {
		return switch (name()) {
		case "boolean" -> Type.BOOLEAN_TYPE;
		case "byte" -> Type.BYTE_TYPE;
		case "short" -> Type.SHORT_TYPE;
		case "int" -> Type.INT_TYPE;
		case "long" -> Type.LONG_TYPE;
		case "float" -> Type.FLOAT_TYPE;
		case "double" -> Type.DOUBLE_TYPE;
		default -> Type.getObjectType(internalName());
		};
	}
}
