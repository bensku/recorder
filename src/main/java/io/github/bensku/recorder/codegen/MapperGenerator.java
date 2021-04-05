package io.github.bensku.recorder.codegen;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import io.github.bensku.recorder.table.Column;
import io.github.bensku.recorder.table.Table;

import static org.objectweb.asm.Opcodes.*;

public class MapperGenerator {
		
	private static final String READ_NAME, READ_DESC;
	private static final String WRITE_NAME, WRITE_DESC;

	static {
		READ_NAME = "read";
		WRITE_NAME = "write";

		try {
			READ_DESC = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod(READ_NAME, ResultSet.class));
			WRITE_DESC = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod(WRITE_NAME, PreparedStatement.class, Record.class));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Generates bytecode for a record mapper.
	 * @param table Table {@link TableSource generated} from the record.
	 * @return Bytecode for a class that can be loaded.
	 */
	public byte[] generate(Table table) {
		ClassWriter cw = new ClassWriter(0);
		// Ignoring generic signature; we could generate one but don't really need it
		cw.visit(V16, ACC_PUBLIC, table.record().internalName() + "$RecordMapper", null, Type.getInternalName(Object.class),
				new String[] {Type.getInternalName(RecordMapper.class)});

		// Make a constructor
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0); // this
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false); // super()
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);

		// Add methods to read/write
		createRead(cw, table.columns(), table.record().internalName());
		createWrite(cw, table.columns(), table.record().internalName());

		return cw.toByteArray();
	}

	private void createRead(ClassWriter cw, List<Column> columns, String recordType) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, READ_NAME, READ_DESC, null, null);
		mv.visitCode();
		
		// Create new record (do not instantiate yet)
		mv.visitTypeInsn(NEW, recordType);
		mv.visitInsn(DUP);

		// Read ResultSet values to stack
		int stack = 4;
		for (int i = 0; i < columns.size(); i++) {
			Column column = columns.get(i);
			mv.visitVarInsn(ALOAD, 1); // ResultSet
			mv.visitLdcInsn(i); // Argument index

			column.type().emitRead(mv);
			stack += column.type().equals(JavaType.LONG) || column.type().equals(JavaType.DOUBLE) ? 2 : 1;
		}
				
		// Call constructor with values from top of stack
		Type[] arguments = columns.stream()
				.map(Column::type)
				.map(JavaType::toAsmType)
				.toArray(Type[]::new);
		mv.visitMethodInsn(INVOKESPECIAL, recordType, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, arguments), false);
		
		// Take the original record reference (not DUP'd one) and return
		mv.visitInsn(ARETURN);

		mv.visitMaxs(stack, 4);
		mv.visitEnd();
	}

	private void createWrite(ClassWriter cw, List<Column> columns, String recordType) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, WRITE_NAME, WRITE_DESC, null, null);
		mv.visitCode();

		for (int i = 0; i < columns.size(); i++) {
			Column column = columns.get(i);
			mv.visitVarInsn(ALOAD, 1); // PreparedStatement
			mv.visitLdcInsn(i); // Argument index
			
			// Get component from record
			mv.visitVarInsn(ALOAD, 2); // Record
			mv.visitTypeInsn(CHECKCAST, recordType); // Generic method, (check) cast
			mv.visitMethodInsn(INVOKEVIRTUAL, recordType, column.name(),
					Type.getMethodDescriptor(column.type().toAsmType()), false);
			
			column.type().emitWrite(mv);
		}
		
		mv.visitInsn(RETURN); // All methods must return

		mv.visitMaxs(4, 3);
		mv.visitEnd();
	}
}
