package de.siphalor.tweed5.weaver.pojoext.attributes.api;

import java.lang.annotation.*;

/**
 * Defines a default value for an attribute.
 * Default values will be inherited by subentries, but can be overridden individually using {@link Attribute}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
@Repeatable(AttributeDefaults.class)
public @interface AttributeDefault {
	String key();
	String[] defaultValue();
}
