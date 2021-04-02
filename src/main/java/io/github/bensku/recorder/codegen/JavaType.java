package io.github.bensku.recorder.codegen;

import java.lang.reflect.RecordComponent;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import io.github.bensku.recorder.record.RecordFormatException;

public abstract class JavaType {
	
	public static final JavaType BOOLEAN = primitive(boolean.class);
	public static final JavaType BYTE = primitive(byte.class);
	public static final JavaType SHORT = primitive(short.class);
	public static final JavaType CHAR = primitive(char.class);
	public static final JavaType INT = primitive(int.class);
	public static final JavaType LONG = primitive(long.class);
	public static final JavaType FLOAT = primitive(float.class);
	public static final JavaType DOUBLE = primitive(double.class);
	public static final JavaType STRING = primitive(String.class);
	
	private static JavaType primitive(Class<?> type) {
		return new Simple(type.getName().replace('.', '/')) ;
	}
	
	public static JavaType fromSignature(String signature) {
		SignatureReader reader = new SignatureReader(signature);
		var visitor = new SignatureVisitor(Opcodes.ASM9) {
			Class<?> wrapperClass = null;
			String genericParam;
			
			@Override
			public void visitClassType(String name) {
				if (wrapperClass == null) {
					wrapperClass = WrappedType.wrapperClass(name).orElseThrow(()
							-> new RecordFormatException("unknown generic type: " + name));
				} else {
					genericParam = name;
				}
			}
		};
		reader.acceptType(visitor);
		return new WrappedType(visitor.wrapperClass, fromType(visitor.genericParam));
	}
	
	public static JavaType fromType(String name) {
		switch (name) {
		case "boolean":
			return BOOLEAN;
		case "byte":
			return BYTE;
		case "short":
			return SHORT;
		case "char":
			return CHAR;
		case "int":
			return INT;
		case "long":
			return LONG;
		case "float":
			return FLOAT;
		case "double":
			return DOUBLE;
		case "java.lang.String":
			return STRING;
		default:
			return new Simple(name);
		}
	}
	
	public static JavaType fromClass(Class<?> type) {
		return fromType(type.getName().replace('.', '/'));
	}

	public static JavaType fromComponent(RecordComponent component) {
		String signature = component.getGenericSignature();
		if (signature != null) {
			return fromSignature(signature);
		} else {
			return fromClass(component.getType());
		}
	}
	
	static class Simple extends JavaType {
		
		private final String name;
		
		Simple(String name) {
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}
	}
	
	/**
	 * Gets the fully qualified class name.
	 * @return Class name.
	 */
	public abstract String name();
	
	abstract void emitRead(MethodVisitor mv);
	
	abstract void emitWrite(MethodVisitor mv);

}
