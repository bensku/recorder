package io.github.bensku.recorder.record;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This record component is allowed to have null as value. Primitive types
 * cannot be made nullable, but their boxed variants can.
 *
 */
@Retention(RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface Nullable {

}
