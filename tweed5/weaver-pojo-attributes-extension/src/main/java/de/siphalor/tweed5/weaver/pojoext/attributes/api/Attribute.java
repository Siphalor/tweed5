package de.siphalor.tweed5.weaver.pojoext.attributes.api;

import java.lang.annotation.*;

/**
 * Defines an attribute on a config entry.
 * The attribute will not be inherited by subentries.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
@Repeatable(Attributes.class)
public @interface Attribute {
	String key();
	String[] value();
}
