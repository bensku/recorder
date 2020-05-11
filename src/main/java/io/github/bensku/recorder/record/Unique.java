package io.github.bensku.recorder.record;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotated record component must be unique in database.
 *
 */
@Retention(RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface Unique {

}
