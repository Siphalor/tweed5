package de.siphalor.tweed5.construct.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows declaring multiple {@link TweedConstruct} annotations on a single element.
 * @see TweedConstruct
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TweedConstructs {
	TweedConstruct[] value();
}
