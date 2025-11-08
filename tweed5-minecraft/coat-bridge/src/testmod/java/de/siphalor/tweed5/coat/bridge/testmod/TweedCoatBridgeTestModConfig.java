package de.siphalor.tweed5.coat.bridge.testmod;

import de.siphalor.tweed5.coat.bridge.api.TweedCoatAttributes;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatBridgeExtension;
import de.siphalor.tweed5.fabric.helper.api.DefaultTweedMinecraftWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.TweedExtension;
import de.siphalor.tweed5.weaver.pojoext.attributes.api.Attribute;
import de.siphalor.tweed5.weaver.pojoext.validation.api.Validator;
import de.siphalor.tweed5.weaver.pojoext.validation.api.validators.WeavableNumberRangeValidator;
import lombok.*;

@DefaultTweedMinecraftWeaving
@TweedExtension(TweedCoatBridgeExtension.class)
@CompoundWeaving(namingFormat = "kebab_case")
@Data
public class TweedCoatBridgeTestModConfig {
	private String test = "hello world";
	private int someInteger = 123;
	@Validator(value = WeavableNumberRangeValidator.class, config = "-10=..=10")
	private int integerInRange = -5;

	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, value = "textures/block/green_terracotta.png")
	private Greeting serverGreeting = new Greeting("Hello server!", "Server");
	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, value = "textures/block/red_terracotta.png")
	private Greeting clientGreeting = new Greeting("Hello client!", "Client");

	@CompoundWeaving
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode
	@ToString
	public static class Greeting {
		public String greeting;
		public String greeter;
	}
}
