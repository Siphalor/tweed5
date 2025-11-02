package de.siphalor.tweed5.fabric.helper.api;

import de.siphalor.tweed5.annotationinheritance.api.AnnotationInheritance;
import de.siphalor.tweed5.attributesextension.api.AttributesExtension;
import de.siphalor.tweed5.attributesextension.api.serde.filter.AttributesReadWriteFilterExtension;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import de.siphalor.tweed5.defaultextensions.validationfallback.api.ValidationFallbackExtension;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.DefaultWeavingExtensions;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.api.annotation.TweedExtension;
import de.siphalor.tweed5.weaver.pojoext.attributes.api.AttributesPojoWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.serde.api.auto.AutoReadWritePojoWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.serde.api.auto.DefaultReadWriteMappings;
import de.siphalor.tweed5.weaver.pojoext.validation.api.ValidatorsPojoWeavingProcessor;

@AnnotationInheritance(passOn = {
		TweedExtension.class,
		PojoWeavingExtension.class,
		DefaultWeavingExtensions.class,
		DefaultReadWriteMappings.class,
		CompoundWeaving.class
})
@TweedExtension(ReadWriteExtension.class)
@TweedExtension(ValidationExtension.class)
@TweedExtension(ValidationFallbackExtension.class)
@TweedExtension(AttributesExtension.class)
@TweedExtension(AttributesReadWriteFilterExtension.class)
@PojoWeavingExtension(AutoReadWritePojoWeavingProcessor.class)
@PojoWeavingExtension(ValidatorsPojoWeavingProcessor.class)
@PojoWeavingExtension(AttributesPojoWeavingProcessor.class)
@DefaultWeavingExtensions
@DefaultReadWriteMappings
@CompoundWeaving
public @interface DefaultTweedMinecraftWeaving {
}
