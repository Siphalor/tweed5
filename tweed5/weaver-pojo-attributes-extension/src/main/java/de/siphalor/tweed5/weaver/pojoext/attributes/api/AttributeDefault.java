package de.siphalor.tweed5.weaver.pojoext.attributes.api;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
@Repeatable(AttributeDefaults.class)
public @interface AttributeDefault {
	String key();
	String[] defaultValue();
}
