package io.github.bensku.recorder;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@FunctionalInterface
public interface ComponentLambda<R, C> extends Serializable {

	C invoke(R record);

	/**
	 * Looks up the record component referenced by this lambda.
	 * @return Record component.
	 * @throws SecurityException When permissions were denied from this method.
	 */
	default ComponentReference<C> lookupComponent() {
		try {
			Method writeReplaceMethod = this.getClass().getDeclaredMethod("writeReplace");
			writeReplaceMethod.setAccessible(true);
			SerializedLambda serialized = (SerializedLambda) writeReplaceMethod.invoke(this);
			
			Class<?> record = Class.forName(serialized.getImplClass().replace('/', '.'));
			String name = serialized.getImplMethodName();
			@SuppressWarnings("unchecked")
			Class<C> type = (Class<C>) record.getMethod(name).getReturnType();
			return new ComponentReference<>(record, name, type);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
			throw new AssertionError("record component lookup failed", e);
		}
	}
}
