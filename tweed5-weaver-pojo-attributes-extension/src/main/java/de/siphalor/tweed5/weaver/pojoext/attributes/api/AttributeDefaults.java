package de.siphalor.tweed5.weaver.pojoext.attributes.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
public @interface AttributeDefaults {
	AttributeDefault[] value();
}
