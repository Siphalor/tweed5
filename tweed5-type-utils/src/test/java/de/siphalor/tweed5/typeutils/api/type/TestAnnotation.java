package de.siphalor.tweed5.typeutils.api.type;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
@Repeatable(TestAnnotations.class)
public @interface TestAnnotation {
	String value();
}
