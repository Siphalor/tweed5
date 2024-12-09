package de.siphalor.tweed5.weaver.pojo.api.annotation;

import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCompoundConfigEntry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this class as a class that should be woven as a {@link de.siphalor.tweed5.core.api.entry.CompoundConfigEntry}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface CompoundWeaving {
	/**
	 * The naming format to use for this POJO.
	 * Use {@link de.siphalor.tweed5.namingformat.api.NamingFormatProvider} to define naming formats.
	 * @see de.siphalor.tweed5.namingformat.impl.DefaultNamingFormatProvider
	 */
	String namingFormat() default "";

	Class<? extends WeavableCompoundConfigEntry> entryClass() default WeavableCompoundConfigEntry.class;
}
