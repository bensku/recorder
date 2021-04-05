package io.github.bensku.recorder.codegen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import io.github.bensku.recorder.record.ForeignKey;
import io.github.bensku.recorder.record.PrimaryKey;

import static org.objectweb.asm.Opcodes.*;

public class WrappedType extends JavaType {
	
	record Wrapper(
			Class<?> wrapperType,
			BiConsumer<MethodVisitor, JavaType> wrapVisitor,
			BiConsumer<MethodVisitor, JavaType> unwrapVisitor
	) {}

	/**
	 * Allowed wrapper types. These are also the only allowed generic types.
	 */
	private static final Map<String, Wrapper> SUPPORTED_WRAPPERS = new HashMap<>();
	
	/**
	 * Gets a wrapper type based on internal name.
	 * @param internalName Internal name of the wrapper class.
	 * @return A wrapper type, or empty optional.
	 */
	static Optional<Wrapper> wrapperType(String internalName) {
		return Optional.ofNullable(SUPPORTED_WRAPPERS.get(internalName));
	}
	
	/**
	 * Registers a wrapper type.
	 * @param type Wrapper type class.
	 * @param wrap Code generator for wrapping a raw value from JDBC to this type.
	 * @param unwrap Code generator for extracting a raw value from this type
	 * for JDBC usage.
	 */
	private static void registerWrapper(Class<?> type, BiConsumer<MethodVisitor, JavaType> wrap, BiConsumer<MethodVisitor, JavaType> unwrap) {
		SUPPORTED_WRAPPERS.put(type.getName().replace('.', '/'), new Wrapper(type, wrap, unwrap));
	}
	
	static {
		Type object = Type.getType(Object.class);
		String objectInit = Type.getMethodDescriptor(Type.VOID_TYPE, object);
		String objectGetter = Type.getMethodDescriptor(object);
		
		// PrimaryKey
		String primaryKey = Type.getInternalName(PrimaryKey.class);
		registerWrapper(PrimaryKey.class, (mv, wrapped) -> {
			mv.visitMethodInsn(INVOKESPECIAL, primaryKey, "<init>", objectInit, false);
		}, (mv, wrapped) -> {
			mv.visitMethodInsn(INVOKEVIRTUAL, primaryKey, "value", objectGetter, false);
		});
		
		// ForeignKey
		String foreignKey = Type.getInternalName(ForeignKey.class);
		String foreignKeyInit = Type.getMethodDescriptor(Type.VOID_TYPE, object, Type.getType(Class.class));
		registerWrapper(ForeignKey.class, (mv, wrapped) -> {
			mv.visitLdcInsn(wrapped.toAsmType()); // Second parameter, target of the foreign key
			mv.visitMethodInsn(INVOKESPECIAL, foreignKey, "<init>", foreignKeyInit, false);
		}, (mv, wrapped) -> {
			mv.visitMethodInsn(INVOKEVIRTUAL, foreignKey, "value", objectGetter, false);
		});
		
		// Standard library wants us to call static methods instead
		try {
			String optional = Type.getInternalName(Optional.class);
			String ofNullable = Type.getMethodDescriptor(Optional.class.getDeclaredMethod("ofNullable", Object.class));
			String orElse = Type.getMethodDescriptor(Optional.class.getDeclaredMethod("orElse", Object.class));
			registerWrapper(Optional.class, (mv, wrapped) -> {
				// Use ofNullable to turn T or null into Optional<T>
				mv.visitMethodInsn(INVOKESTATIC, optional, "ofNullable", ofNullable, false);
			}, (mv, wrapped) -> {
				// Use Optional<T>.orElse(null) to turn it into T or null
				mv.visitInsn(ACONST_NULL);
				mv.visitMethodInsn(INVOKEVIRTUAL, optional, "orElse", orElse, false);
			});
		} catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError(e);
		}
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
	public String internalName() {
		return wrappedType.internalName();
	}

	@Override
	void emitRead(MethodVisitor mv) {
		wrappedType.emitRead(mv);
		wrapper.wrapVisitor.accept(mv, wrappedType);
	}

	@Override
	void emitWrite(MethodVisitor mv) {
		wrappedType.emitWrite(mv);
		wrapper.unwrapVisitor.accept(mv, wrappedType);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WrappedType o) {
			return o.wrapper == this.wrapper && o.wrappedType.equals(this.wrappedType);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return System.identityHashCode(wrapper) + 31 * wrappedType.hashCode();
	}
	
	@Override
	public String toString() {
		return wrapper.wrapperType.getSimpleName() + "<" + wrappedType.toString() + ">";
	}
}
