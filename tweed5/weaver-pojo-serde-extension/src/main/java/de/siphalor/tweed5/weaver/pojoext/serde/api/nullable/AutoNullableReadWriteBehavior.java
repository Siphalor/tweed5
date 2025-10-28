package de.siphalor.tweed5.weaver.pojoext.serde.api.nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @see AutoNullableReadWritePojoWeavingProcessor
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface AutoNullableReadWriteBehavior {
	AutoReadWriteNullability defaultNullability();
}
