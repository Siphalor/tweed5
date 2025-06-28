package de.siphalor.tweed5.weaver.pojoext.serde.api.auto;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Repeatable(AutoReadWriteMappings.class)
public @interface AutoReadWriteMapping {
	Class<? extends ConfigEntry>[] entryClasses() default { ConfigEntry.class };
	Class<?>[] valueClasses();
	String spec() default "";
	String reader() default "";
	String writer() default "";
}
