package de.siphalor.tweed5.construct.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides more information about a parameter of a {@link TweedConstruct}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstructParameter {
	/**
	 * Allows defining a named parameter with the given name.
	 * Parameters with this set are not considered as typed parameters.
	 */
	String name();
}
