package io.github.bensku.recorder.record;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotated record component is primary key of the table in the database.
 * Only one primary key per table/record may be present.
 *
 */
@Retention(RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface PrimaryKey {

	boolean generated() default true;
}
