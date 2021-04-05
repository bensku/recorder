package io.github.bensku.recorder.codegen;

import java.lang.reflect.RecordComponent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.Consumer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import io.github.bensku.recorder.record.RecordFormatException;

import static org.objectweb.asm.Opcodes.*;

public abstract class JavaType {
	
	private static final String RESULT_SET, PREPARED_STATEMENT;

	private static final String BOOLEAN_GET, BOOLEAN_SET;
	private static final String BYTE_GET, BYTE_SET;
	private static final String SHORT_GET, SHORT_SET;
	private static final String INT_GET, INT_SET;
	private static final String LONG_GET, LONG_SET;
	private static final String FLOAT_GET, FLOAT_SET;
	private static final String DOUBLE_GET, DOUBLE_SET;
	private static final String STRING_GET, STRING_SET;
	private static final String OBJECT_GET, OBJECT_SET;
	
	// Prepare type descriptors needed for codegen at class load
	// If relevant methods don't exist, this should crash as early as possible
	static {
		try {
			RESULT_SET = Type.getInternalName(ResultSet.class);
			PREPARED_STATEMENT = Type.getInternalName(PreparedStatement.class);

			BOOLEAN_GET = Type.getMethodDescriptor(ResultSet.class.getDeclaredMethod("getBoolean", int.class));
			BOOLEAN_SET = Type.getMethodDescriptor(PreparedStatement.class.getDeclaredMethod("setBoolean", int.class, boolean.class));

			BYTE_GET = Type.getMethodDescriptor(ResultSet.class.getDeclaredMethod("getByte", int.class));
			BYTE_SET = Type.getMethodDescriptor(PreparedStatement.class.getDeclaredMethod("setByte", int.class, byte.class));

			SHORT_GET = Type.getMethodDescriptor(ResultSet.class.getDeclaredMethod("getShort", int.class));
			SHORT_SET = Type.getMethodDescriptor(PreparedStatement.class.getDeclaredMethod("setShort", int.class, short.class));

			INT_GET = Type.getMethodDescriptor(ResultSet.class.getDeclaredMethod("getInt", int.class));
			INT_SET = Type.getMethodDescriptor(PreparedStatement.class.getDeclaredMethod("setInt", int.class, int.class));

			LONG_GET = Type.getMethodDescriptor(ResultSet.class.getDeclaredMethod("getLong", int.class));
			LONG_SET = Type.getMethodDescriptor(PreparedStatement.class.getDeclaredMethod("setLong", int.class, long.class));

			FLOAT_GET = Type.getMethodDescriptor(ResultSet.class.getDeclaredMethod("getFloat", int.class));
			FLOAT_SET = Type.getMethodDescriptor(PreparedStatement.class.getDeclaredMethod("setFloat", int.class, float.class));

			DOUBLE_GET = Type.getMethodDescriptor(ResultSet.class.getDeclaredMethod("getDouble", int.class));
			DOUBLE_SET = Type.getMethodDescriptor(PreparedStatement.class.getDeclaredMethod("setDouble", int.class, double.class));
			
			STRING_GET = Type.getMethodDescriptor(ResultSet.class.getDeclaredMethod("getString", int.class));
			STRING_SET = Type.getMethodDescriptor(PreparedStatement.class.getDeclaredMethod("setString", int.class, String.class));

			OBJECT_GET = Type.getMethodDescriptor(ResultSet.class.getDeclaredMethod("getObject", int.class, Class.class));
			OBJECT_SET = Type.getMethodDescriptor(PreparedStatement.class.getDeclaredMethod("setObject", int.class, Object.class));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError(e);
		}
	}
	
	// Primitive types (and j.l.String, because JDBC has get/set for it)
	
	public static final JavaType BOOLEAN = primitive(boolean.class, Type.BOOLEAN_TYPE, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getBoolean", BOOLEAN_GET, true);
	}, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, PREPARED_STATEMENT, "setBoolean", BOOLEAN_SET, true);
	});
	public static final JavaType BYTE = primitive(byte.class, Type.BYTE_TYPE, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getByte", BYTE_GET, true);
	}, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, PREPARED_STATEMENT, "setByte", BYTE_SET, true);
	});
	public static final JavaType SHORT = primitive(short.class, Type.SHORT_TYPE, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getShort", SHORT_GET, true);
	}, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, PREPARED_STATEMENT, "setShort", SHORT_SET, true);
	});
	public static final JavaType INT = primitive(int.class, Type.INT_TYPE, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getInt", INT_GET, true);
	}, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, PREPARED_STATEMENT, "setInt", INT_SET, true);
	});
	public static final JavaType LONG = primitive(long.class, Type.LONG_TYPE, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getLong", LONG_GET, true);
	}, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, PREPARED_STATEMENT, "setLong", LONG_SET, true);
	});
	public static final JavaType FLOAT = primitive(float.class, Type.FLOAT_TYPE, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getFloat", FLOAT_GET, true);
	}, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, PREPARED_STATEMENT, "setFloat", FLOAT_SET, true);
	});
	public static final JavaType DOUBLE = primitive(double.class, Type.DOUBLE_TYPE, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getDouble", DOUBLE_GET, true);
	}, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, PREPARED_STATEMENT, "setDouble", DOUBLE_SET, true);
	});
	public static final JavaType STRING = primitive(String.class, Type.getObjectType("java/lang/String"), (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getString", STRING_GET, true);
	}, (mv) -> {
		mv.visitMethodInsn(INVOKEINTERFACE, PREPARED_STATEMENT, "setString", STRING_SET, true);
	});
	
	private static JavaType primitive(Class<?> type, Type asmType, Consumer<MethodVisitor> readVisitor, Consumer<MethodVisitor> writeVisitor) {
		return new Primitive(type.getName(), asmType, readVisitor, writeVisitor) ;
	}
	
	/**
	 * Creates a type from a generic method signature.
	 * @param signature Method signature.
	 * @return Java type.
	 * @throws RecordFormatException If the generic type is unsupported.
	 */
	public static JavaType fromSignature(String signature) {
		SignatureReader reader = new SignatureReader(signature);
		var visitor = new SignatureVisitor(Opcodes.ASM9) {
			WrappedType.Wrapper wrapper = null;
			String genericParam;
			
			@Override
			public void visitClassType(String name) {
				if (wrapper == null) {
					wrapper = WrappedType.wrapperType(name).orElseThrow(()
							-> new RecordFormatException("unknown generic type: " + name));
				} else {
					genericParam = name;
				}
			}
		};
		reader.acceptType(visitor);
		return new WrappedType(visitor.wrapper, fromName(visitor.genericParam));
	}
	
	/**
	 * Creates a type from a class name.
	 * @param name Fully qualified class name.
	 * @return Java type.
	 */
	public static JavaType fromName(String name) {
		switch (name) {
		case "boolean":
			return BOOLEAN;
		case "byte":
			return BYTE;
		case "short":
			return SHORT;
		case "char":
			throw new UnsupportedOperationException("char is not mapped to any JDBC type");
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
	
	/**
	 * Creates a type from record component field descriptor.
	 * @param desc Field descriptor of a record component.
	 * @return Java type.
	 */
	public static JavaType fromDescriptor(String desc) {
		return fromName(Type.getType(desc).getClassName());
	}
	
	/**
	 * Creates a Java type based on class.
	 * @param type Java class.
	 * @return Recorder Java type.
	 */
	public static JavaType fromClass(Class<?> type) {
		return fromName(type.getName());
	}

	/**
	 * Creates a Java type for a record component. Unlike
	 * {@link #fromClass(Class)}, this supports generic wrapper types.
	 * @param component Java record component.
	 * @return Java type.
	 */
	public static JavaType fromComponent(RecordComponent component) {
		String signature = component.getGenericSignature();
		if (signature != null) {
			return fromSignature(signature);
		} else {
			return fromClass(component.getType());
		}
	}
	
	/**
	 * Simple object type.
	 *
	 */
	static class Simple extends JavaType {
		
		private final String name;
		private final String internalName;
		
		Simple(String name) {
			this.name = name;
			this.internalName = name.replace('.', '/');
		}

		@Override
		public String name() {
			return name;
		}
		
		@Override
		public String internalName() {
			return internalName;
		}

		@Override
		void emitRead(MethodVisitor mv) {
			mv.visitLdcInsn(toAsmType()); // Wanted type as second parameter
			mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getObject", OBJECT_GET, true);
			mv.visitTypeInsn(CHECKCAST, internalName); // In bytecode, generic return is just Object
		}

		@Override
		void emitWrite(MethodVisitor mv) {
			mv.visitMethodInsn(INVOKEINTERFACE, PREPARED_STATEMENT, "setObject", OBJECT_SET, true);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Simple o) {
				return o.name.equals(this.name);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	/**
	 * A primitive type with custom {@link #emitRead(MethodVisitor)} and
	 * {@link #emitWrite(MethodVisitor)}.
	 *
	 */
	static class Primitive extends Simple {
		
		private final Type asmType;
		private final Consumer<MethodVisitor> readVisitor;
		private final Consumer<MethodVisitor> writeVisitor;
		
		Primitive(String name, Type asmType, Consumer<MethodVisitor> readVisitor, Consumer<MethodVisitor> writeVisitor) {
			super(name);
			this.asmType = asmType;
			this.readVisitor = readVisitor;
			this.writeVisitor = writeVisitor;
		}
		
		@Override
		void emitRead(MethodVisitor mv) {
			readVisitor.accept(mv);
		}
		
		@Override
		void emitWrite(MethodVisitor mv) {
			writeVisitor.accept(mv);
		}
		
		@Override
		Type toAsmType() {
			return asmType;
		}
		
		// Inherit equals() and hashCode() from Simple, they are good enough
	}
	
	/**
	 * Gets the internal name of this class.
	 * @return Class name.
	 */
	public abstract String name();
	
	/**
	 * Gets the internal name of this class.
	 * @return Internal name, e.g. 'java/lang/Object' or 'boolean'.
	 */
	public abstract String internalName();
	
	/**
	 * Emits a read from {@link ResultSet} for this type.
	 * Stack: ResultSet, index -> value
	 * @param mv Method visitor.
	 */
	abstract void emitRead(MethodVisitor mv);
	
	/**
	 * Emits a write to {@link PreparedStatement} for this type.
	 * Stack: PreparedStatement, index, value -> (empty)
	 * @param mv Method visitor.
	 */
	abstract void emitWrite(MethodVisitor mv);
	
	Type toAsmType() {
		return Type.getObjectType(internalName());
	}

}
