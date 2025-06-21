package de.siphalor.tweed5.weaver.pojo.api.annotation;

import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeavingExtension;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Repeatable(PojoWeavingExtensions.class)
public @interface PojoWeavingExtension {
	Class<? extends TweedPojoWeavingExtension> value();
}
