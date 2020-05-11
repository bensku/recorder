package io.github.bensku.recorder.record;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Overrides name of the database table used to store this record type.
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface TableName {

	String value();
}
