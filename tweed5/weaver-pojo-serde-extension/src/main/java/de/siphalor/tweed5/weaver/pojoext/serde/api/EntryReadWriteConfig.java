package de.siphalor.tweed5.weaver.pojoext.serde.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code <spec> = <id> [ "(" <spec> ( "," <spec> )* ")" ] }
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EntryReadWriteConfig {
	String value() default "";
	String writer() default "";
	String reader() default "";
}
