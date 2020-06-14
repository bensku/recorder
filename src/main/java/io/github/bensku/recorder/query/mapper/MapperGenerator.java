package io.github.bensku.recorder.query.mapper;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.Arrays;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import io.github.bensku.recorder.sql.Column;
import io.github.bensku.recorder.sql.JavaType;
import io.github.bensku.recorder.sql.Table;

public class MapperGenerator implements Opcodes {

	private static final String READ_NAME, READ_DESC;
	private static final String WRITE_NAME, WRITE_DESC;

	private static final String RESULT_SET;

	private static final String BOOLEAN_GET, BOOLEAN_SET;
	private static final String BYTE_GET, BYTE_SET;
	private static final String SHORT_GET, SHORT_SET;
	private static final String INT_GET, INT_SET;
	private static final String LONG_GET, LONG_SET;
	private static final String FLOAT_GET, FLOAT_SET;
	private static final String DOUBLE_GET, DOUBLE_SET;
	private static final String OBJECT_GET, OBJECT_SET;

	static {
		READ_NAME = "read";
		WRITE_NAME = "write";

		try {
			READ_DESC = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod(READ_NAME, ResultSet.class));
			WRITE_DESC = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod(WRITE_NAME, ResultSet.class));

			RESULT_SET = Type.getInternalName(ResultSet.class);

			BOOLEAN_GET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("getBoolean"));
			BOOLEAN_SET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("setBoolean", boolean.class));

			BYTE_GET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("getByte"));
			BYTE_SET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("setByte", byte.class));

			SHORT_GET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("getShort"));
			SHORT_SET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("setShort", short.class));

			INT_GET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("getInt"));
			INT_SET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("setInt", int.class));

			LONG_GET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("getLong"));
			LONG_SET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("setLong", long.class));

			FLOAT_GET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("getFloat"));
			FLOAT_SET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("setFloat", float.class));

			DOUBLE_GET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("getDouble"));
			DOUBLE_SET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("setDouble", double.class));

			OBJECT_GET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("getObject"));
			OBJECT_SET = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod("setObject", Object.class));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError(e);
		}
	}

	public RecordMapper<?> create(Table table, Class<? extends Record> recordType) {
		String name = table.name() + "$RecordMapper";
		byte[] code = createMapper(name, table.columns(), Type.getInternalName(recordType));

		// Load class
		// TODO once JDK 15 lands, use loadAnonymousClass (allows GC to work)
		try {
			Class<?> clazz = MethodHandles.lookup().defineClass(code);
			return (RecordMapper<?>) clazz.getConstructor().newInstance();
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new AssertionError("loading created mapper failed", e);
		}
	}

	/**
	 * Creates record mapper bytecode.
	 * @param name Name of record mapper class.
	 * @param columns Columns to map.
	 * @param recordType Internal name of record type.
	 * @return Bytecode of mapper.
	 */
	private byte[] createMapper(String name, Column[] columns, String recordType) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// Ignoring generic signature, we COULD create one but don't need it
		cw.visit(V14, ACC_PUBLIC, name, null, Type.getInternalName(Object.class),
				new String[] {Type.getInternalName(RecordMapper.class)});

		// Make a constructor
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitVarInsn(ALOAD, 0); // this
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false); // super()
		mv.visitInsn(RETURN);

		// Add methods to read/write
		createRead(cw, columns, recordType);
		createWrite(cw, columns);

		return cw.toByteArray();
	}

	private void createRead(ClassWriter cw, Column[] columns, String recordType) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, READ_NAME, READ_DESC, null, null);
		mv.visitCode();

		// Read ResultSet values to stack
		for (int i = 0; i < columns.length; i++) {
			mv.visitVarInsn(ALOAD, 1); // ResultSet
			mv.visitLdcInsn(i); // Argument index

			JavaType type = columns[i].type();
			if (type.equals(JavaType.BOOLEAN)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "setBoolean", BOOLEAN_SET, true);
			} else if (type.equals(JavaType.BYTE)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "setByte", BYTE_SET, true);
			} else if (type.equals(JavaType.SHORT)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "setShort", SHORT_SET, true);
			} else if (type.equals(JavaType.INT)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "setInt", INT_SET, true);
			} else if (type.equals(JavaType.LONG)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "setLong", LONG_SET, true);
			} else if (type.equals(JavaType.FLOAT)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "setFloat", FLOAT_SET, true);
			} else if (type.equals(JavaType.DOUBLE)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "setDouble", DOUBLE_SET, true);
			} else {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "setObject", OBJECT_SET, true);
			}
		}
		
		// Create new record (do not instantiate yet)
		mv.visitTypeInsn(NEW, recordType);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, 2); // For returning from this method
		
		// Call constructor (will instantiate values)
		Type[] arguments = Arrays.stream(columns)
				.map(Column::type)
				.map(JavaType::internalName)
				.map(Type::getType)
				.toArray(Type[]::new);
		mv.visitMethodInsn(INVOKESPECIAL, recordType, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, arguments), false);
		
		// Load from stack and return
		mv.visitVarInsn(ALOAD, 2);
		mv.visitInsn(RETURN);

		mv.visitMaxs(-1, -1);
		mv.visitEnd();
	}

	private void createWrite(ClassWriter cw, Column[] columns) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, WRITE_NAME, WRITE_DESC, null, null);
		mv.visitCode();

		for (int i = 0; i < columns.length; i++) {
			mv.visitVarInsn(ALOAD, 1); // ResultSet
			mv.visitLdcInsn(i); // Argument index
			
			// TODO read record component

			JavaType type = columns[i].type();
			if (type.equals(JavaType.BOOLEAN)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getBoolean", BOOLEAN_GET, true);
			} else if (type.equals(JavaType.BYTE)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getByte", BYTE_GET, true);
			} else if (type.equals(JavaType.SHORT)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getShort", SHORT_GET, true);
			} else if (type.equals(JavaType.INT)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getInt", INT_GET, true);
			} else if (type.equals(JavaType.LONG)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getLong", LONG_GET, true);
			} else if (type.equals(JavaType.FLOAT)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getFloat", FLOAT_GET, true);
			} else if (type.equals(JavaType.DOUBLE)) {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getDouble", DOUBLE_GET, true);
			} else {
				mv.visitMethodInsn(INVOKEINTERFACE, RESULT_SET, "getObject", OBJECT_GET, true);
			}
		}
		
		mv.visitInsn(RETURN); // All methods must return

		mv.visitMaxs(-1, -1);
		mv.visitEnd();
	}
}
