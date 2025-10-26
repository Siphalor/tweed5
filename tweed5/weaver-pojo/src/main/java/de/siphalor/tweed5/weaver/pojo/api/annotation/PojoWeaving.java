package de.siphalor.tweed5.weaver.pojo.api.annotation;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.weaver.pojo.api.weaving.CompoundPojoWeaver;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TrivialPojoWeaver;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeavingExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface PojoWeaving {
	Class<? extends ConfigContainer> container() default DefaultConfigContainer.class;

	Class<? extends TweedExtension>[] extensions() default {};
}
