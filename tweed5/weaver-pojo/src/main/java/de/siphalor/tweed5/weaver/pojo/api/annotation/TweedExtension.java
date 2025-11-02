package de.siphalor.tweed5.weaver.pojo.api.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Repeatable(TweedExtensions.class)
public @interface TweedExtension {
	Class<? extends de.siphalor.tweed5.core.api.extension.TweedExtension> value();
}
