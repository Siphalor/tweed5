package de.siphalor.tweed5.weaver.pojo.api.annotation;

import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCollectionConfigEntry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.TYPE_USE})
public @interface CollectionWeaving {
	Class<? extends WeavableCollectionConfigEntry> entryClass() default WeavableCollectionConfigEntry.class;
}
