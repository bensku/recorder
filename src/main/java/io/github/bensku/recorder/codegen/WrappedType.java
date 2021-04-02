package io.github.bensku.recorder.codegen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import io.github.bensku.recorder.record.ForeignKey;
import io.github.bensku.recorder.record.PrimaryKey;

public class WrappedType extends JavaType implements Opcodes {
	
	record Wrapper(
			Class<?> wrapperType,
			Consumer<MethodVisitor> wrapVisitor,
			Consumer<MethodVisitor> unwrapVisitor
	) {}

	private static final Map<String, Wrapper> WRAPPER_WHITELIST = new HashMap<>();
	
	static Optional<Wrapper> wrapperClass(String internalName) {
		return Optional.ofNullable(WRAPPER_WHITELIST.get(internalName));
	}
	
	private static void registerWrapper(Class<?> type, Consumer<MethodVisitor> wrap, Consumer<MethodVisitor> unwrap) {
		WRAPPER_WHITELIST.put(type.getName().replace('.', '/'), new Wrapper(type, wrap, unwrap));
	}
	
	static {
		// TODO figure out visibility of key constructors
		// Can we use nestmates to access BOTH ForeignKey and PrimaryKey?
		Type object = Type.getObjectType("java/lang/Object");
		String objectInit = Type.getMethodDescriptor(Type.VOID_TYPE, object);
		String objectGetter = Type.getMethodDescriptor(object);
		
		String primaryKey = PrimaryKey.class.getName().replace('.', '/');
		registerWrapper(PrimaryKey.class, (mv) -> {
			mv.visitMethodInsn(INVOKESPECIAL, primaryKey, "<init>", objectInit, false);
		}, (mv) -> {
			mv.visitMethodInsn(INVOKEVIRTUAL, primaryKey, "value", objectGetter, false);
		});
		
		String foreignKey = ForeignKey.class.getName().replace('.', '/');
		registerWrapper(ForeignKey.class, (mv) -> {
			mv.visitMethodInsn(INVOKESPECIAL, foreignKey, "<init>", objectInit, false);
		}, (mv) -> {
			mv.visitMethodInsn(INVOKEVIRTUAL, foreignKey, "value", objectGetter, false);
		});
		
		// Standard library optional works a bit differently
		String optional = Optional.class.getName().replace('.', '/');
		String ofNullable = Type.getMethodDescriptor(Type.getType(optional), object);
		String orElse = Type.getMethodDescriptor(object, object);
		registerWrapper(Optional.class, (mv) -> {
			// Use ofNullable to turn T or null into Optional<T>
			mv.visitMethodInsn(INVOKESTATIC, optional, "ofNullable", ofNullable, false);
		}, (mv) -> {
			// Use Optional<T>.orElse(null) to turn it into T or null
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKEVIRTUAL, optional, "orElse", orElse, false);
		});
	}
	
	private Wrapper wrapper;
	private JavaType wrappedType;
	
	WrappedType(Wrapper wrapper, JavaType type) {
		this.wrapper = wrapper;
		this.wrappedType = type;
	}
	
	public Class<?> wrapperClass() {
		return wrapper.wrapperType;
	}
	
	public JavaType wrappedType() {
		return wrappedType;
	}

	@Override
	public String name() {
		return wrappedType.name();
	}

	@Override
	void emitRead(MethodVisitor mv) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void emitWrite(MethodVisitor mv) {
		// TODO Auto-generated method stub
		
	}
}
