package io.github.bensku.recorder.query.mapper;

import java.lang.reflect.RecordComponent;
import java.sql.ResultSet;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MapperGenerator implements Opcodes {
	
	private static final String READ_NAME, READ_DESC;
	private static final String WRITE_NAME, WRITE_DESC;
	
	static {
		READ_NAME = "read";
		WRITE_NAME = "write";
		
		try {
			READ_DESC = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod(READ_NAME, ResultSet.class));
			WRITE_DESC = Type.getMethodDescriptor(RecordMapper.class.getDeclaredMethod(WRITE_NAME, ResultSet.class));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError(e);
		}
	}

	public <R extends Record> RecordMapper<R> create(Class<R> type) {
		String name = type.getName() + "$RecordMapper";
		byte[] code = createMapper(name, type.getRecordComponents());
	}
	
	private byte[] createMapper(String name, RecordComponent[] components) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// Ignoring generic signature, we COULD create one but don't need it
		cw.visit(V14, ACC_PUBLIC, name, null, Type.getInternalName(Object.class),
				new String[] {Type.getInternalName(RecordMapper.class)});
		
		createRead(cw, components);
		createWrite(cw, components);
	}
	
	private void createRead(ClassWriter cw, RecordComponent[] components) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, READ_NAME, READ_DESC, null, null);
		
		
		
		mv.visitMaxs(-1, -1);
		mv.visitEnd();
	}
	
	private void createWrite(ClassWriter cw, RecordComponent[] components) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, WRITE_NAME, WRITE_DESC, null, null);

		mv.visitMaxs(-1, -1);
		mv.visitEnd();
	}
}
