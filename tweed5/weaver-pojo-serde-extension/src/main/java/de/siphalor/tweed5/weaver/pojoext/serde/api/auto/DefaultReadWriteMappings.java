package de.siphalor.tweed5.weaver.pojoext.serde.api.auto;

import de.siphalor.tweed5.annotationinheritance.api.AnnotationInheritance;
import de.siphalor.tweed5.core.api.entry.CollectionConfigEntry;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableStringMapConfigEntry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;

@AnnotationInheritance(passOn = AutoReadWriteMapping.class)
@AutoReadWriteMapping(valueClasses = {boolean.class, Boolean.class}, spec = "tweed5.bool")
@AutoReadWriteMapping(valueClasses = {byte.class, Byte.class}, spec = "tweed5.byte")
@AutoReadWriteMapping(valueClasses = {short.class, Short.class}, spec = "tweed5.short")
@AutoReadWriteMapping(valueClasses = {int.class, Integer.class}, spec = "tweed5.integer")
@AutoReadWriteMapping(valueClasses = {long.class, Long.class}, spec = "tweed5.long")
@AutoReadWriteMapping(valueClasses = {float.class, Float.class}, spec = "tweed5.float")
@AutoReadWriteMapping(valueClasses = {double.class, Double.class}, spec = "tweed5.double")
@AutoReadWriteMapping(valueClasses = String.class, spec = "tweed5.string")
@AutoReadWriteMapping(valueClasses = Enum.class, spec = "tweed5.enum")
@AutoReadWriteMapping(
		entryClasses = CollectionConfigEntry.class,
		valueClasses = Collection.class,
		spec = "tweed5.collection"
)
@AutoReadWriteMapping(
		entryClasses = CompoundConfigEntry.class,
		valueClasses = Object.class,
		spec = "tweed5.compound"
)
@AutoReadWriteMapping(
		entryClasses = WeavableStringMapConfigEntry.class,
		valueClasses = Map.class,
		spec = "tweed5.mutatableStruct"
)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface DefaultReadWriteMappings {
}
