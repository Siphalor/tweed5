package de.siphalor.tweed5.weaver.pojo.api.annotation;

import de.siphalor.tweed5.annotationinheritance.api.AnnotationInheritance;
import de.siphalor.tweed5.weaver.pojo.api.weaving.CollectionPojoWeaver;
import de.siphalor.tweed5.weaver.pojo.api.weaving.CompoundPojoWeaver;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TrivialPojoWeaver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@AnnotationInheritance(passOn = PojoWeavingExtension.class)
@PojoWeavingExtension(CompoundPojoWeaver.class)
@PojoWeavingExtension(CollectionPojoWeaver.class)
@PojoWeavingExtension(TrivialPojoWeaver.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface DefaultWeavingExtensions {
}
