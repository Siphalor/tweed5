package de.siphalor.tweed5.weaver.pojo.api.annotation;

import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableStringMapConfigEntry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.TYPE_USE})
public @interface StringMapWeaving {
	Class<? extends WeavableStringMapConfigEntry> entryClass() default WeavableStringMapConfigEntry.class;
}
