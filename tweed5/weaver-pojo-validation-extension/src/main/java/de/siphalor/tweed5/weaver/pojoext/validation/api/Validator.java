package de.siphalor.tweed5.weaver.pojoext.validation.api;

import de.siphalor.tweed5.weaver.pojoext.validation.api.validators.WeavableConfigEntryValidator;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.TYPE_USE, ElementType.ANNOTATION_TYPE})
@Repeatable(Validators.class)
public @interface Validator {
	Class<? extends WeavableConfigEntryValidator> value();
	String config() default "";
}
