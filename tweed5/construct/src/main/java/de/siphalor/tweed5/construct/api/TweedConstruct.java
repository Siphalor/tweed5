package de.siphalor.tweed5.construct.api;

import java.lang.annotation.*;

/**
 * Indicates a method or constructor that should be used for construction using {@link TweedConstructFactory}.
 * <p>
 * There must only be a single annotation for a certain target class on any constructor or static method of a class.
 */
@Repeatable(TweedConstructs.class)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TweedConstruct {
	/**
	 * Defines the target base class that this constructor may be used to create.
	 * This is the base class defined in {@link TweedConstructFactory#builder(Class)}.
	 */
	Class<?> value();
}
