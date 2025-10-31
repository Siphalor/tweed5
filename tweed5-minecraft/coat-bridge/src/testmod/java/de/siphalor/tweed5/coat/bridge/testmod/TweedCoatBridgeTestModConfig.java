package de.siphalor.tweed5.coat.bridge.testmod;

import de.siphalor.tweed5.attributesextension.api.AttributesExtension;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatAttributes;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatBridgeExtension;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.DefaultWeavingExtensions;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojoext.attributes.api.Attribute;
import de.siphalor.tweed5.weaver.pojoext.attributes.api.AttributesPojoWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.serde.api.auto.AutoReadWritePojoWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.serde.api.auto.DefaultReadWriteMappings;
import de.siphalor.tweed5.weaver.pojoext.validation.api.Validator;
import de.siphalor.tweed5.weaver.pojoext.validation.api.ValidatorsPojoWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.validation.api.validators.WeavableNumberRangeValidator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@PojoWeaving(extensions = {
		ReadWriteExtension.class,
		TweedCoatBridgeExtension.class,
		ValidationExtension.class,
		AttributesExtension.class,
})
@PojoWeavingExtension(AutoReadWritePojoWeavingProcessor.class)
@PojoWeavingExtension(ValidatorsPojoWeavingProcessor.class)
@PojoWeavingExtension(AttributesPojoWeavingProcessor.class)
@DefaultWeavingExtensions
@DefaultReadWriteMappings
@CompoundWeaving(namingFormat = "kebab_case")
@Data
public class TweedCoatBridgeTestModConfig {
	private String test = "hello world";
	private int someInteger = 123;
	@Validator(value = WeavableNumberRangeValidator.class, config = "-10=..=10")
	private int integerInRange = -5;

	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, values = "textures/block/green_terracotta.png")
	private Greeting serverGreeting = new Greeting("Hello server!", "Server");
	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, values = "textures/block/red_terracotta.png")
	private Greeting clientGreeting = new Greeting("Hello client!", "Client");

	@NoArgsConstructor
	@AllArgsConstructor
	@CompoundWeaving
	public static class Greeting {
		public String greeting;
		public String greeter;
	}
}
