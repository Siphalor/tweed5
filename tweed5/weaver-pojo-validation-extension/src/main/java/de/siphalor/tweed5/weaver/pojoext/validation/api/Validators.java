package de.siphalor.tweed5.weaver.pojoext.validation.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.TYPE_USE, ElementType.ANNOTATION_TYPE})
public @interface Validators {
	Validator[] value();
}
