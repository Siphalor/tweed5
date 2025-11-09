package de.siphalor.tweed5.fabric.helper.api;

import de.siphalor.tweed5.annotationinheritance.api.AnnotationInheritance;
import de.siphalor.tweed5.attributesextension.api.AttributesExtension;
import de.siphalor.tweed5.attributesextension.api.serde.filter.AttributesReadWriteFilterExtension;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import de.siphalor.tweed5.defaultextensions.validationfallback.api.ValidationFallbackExtension;
import de.siphalor.tweed5.weaver.pojo.api.annotation.*;
import de.siphalor.tweed5.weaver.pojoext.attributes.api.AttributesPojoWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.presets.api.DefaultPresetWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.presets.api.PresetsWeavingProcessor;
import de.siphalor.tweed5.defaultextensions.readfallback.api.ReadFallbackExtension;
import de.siphalor.tweed5.weaver.pojoext.serde.api.ReadWritePojoWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.serde.api.auto.AutoReadWritePojoWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.serde.api.auto.DefaultReadWriteMappings;
import de.siphalor.tweed5.weaver.pojoext.validation.api.ValidatorsPojoWeavingProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@AnnotationInheritance(passOn = {
		PojoWeaving.class,
		TweedExtension.class,
		PojoWeavingExtension.class,
		DefaultWeavingExtensions.class,
		DefaultReadWriteMappings.class,
		CompoundWeaving.class
})
@PojoWeaving
@TweedExtension(ReadWriteExtension.class)
@TweedExtension(ReadFallbackExtension.class)
@TweedExtension(PresetsExtension.class)
@TweedExtension(ValidationExtension.class)
@TweedExtension(ValidationFallbackExtension.class)
@TweedExtension(AttributesExtension.class)
@TweedExtension(AttributesReadWriteFilterExtension.class)
@PojoWeavingExtension(AutoReadWritePojoWeavingProcessor.class)
@PojoWeavingExtension(ReadWritePojoWeavingProcessor.class)
@PojoWeavingExtension(ValidatorsPojoWeavingProcessor.class)
@PojoWeavingExtension(AttributesPojoWeavingProcessor.class)
@PojoWeavingExtension(PresetsWeavingProcessor.class)
@PojoWeavingExtension(DefaultPresetWeavingProcessor.class)
@DefaultWeavingExtensions
@DefaultReadWriteMappings
@CompoundWeaving
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface DefaultTweedMinecraftWeaving {
}
