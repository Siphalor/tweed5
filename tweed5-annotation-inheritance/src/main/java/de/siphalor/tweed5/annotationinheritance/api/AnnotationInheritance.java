package de.siphalor.tweed5.annotationinheritance.api;

import java.lang.annotation.*;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotationInheritance {
	Class<? extends Annotation>[] passOn() default {};
	Class<? extends Annotation>[] override() default {};
}
