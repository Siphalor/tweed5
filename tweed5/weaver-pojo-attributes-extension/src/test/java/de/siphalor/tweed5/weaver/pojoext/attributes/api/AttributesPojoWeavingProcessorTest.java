package de.siphalor.tweed5.weaver.pojoext.attributes.api;

import de.siphalor.tweed5.attributesextension.api.AttributesExtension;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.weaver.pojo.api.TweedPojoWeaver;
import de.siphalor.tweed5.weaver.pojo.api.annotation.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class AttributesPojoWeavingProcessorTest {
	@Test
	void test() {
		ConfigContainer<Config> configContainer = TweedPojoWeaver.forClass(Config.class).weave();
		configContainer.initialize();

		AttributesExtension attributesExtension = configContainer.extension(AttributesExtension.class).orElseThrow();

		assertThat(configContainer.rootEntry()).asInstanceOf(type(CompoundConfigEntry.class)).satisfies(
				compoundEntry -> assertThat(compoundEntry.subEntries().get("string"))
						.asInstanceOf(type(ConfigEntry.class)).satisfies(
								entry -> assertThat(attributesExtension.getAttributeValues(entry, "scope"))
										.isEqualTo(List.of("game"))
						),
				compoundEntry -> assertThat(compoundEntry.subEntries().get("sub1"))
						.asInstanceOf(type(CompoundConfigEntry.class)).satisfies(
								subEntry -> assertThat(attributesExtension.getAttributeValues(subEntry, "color"))
										.isEqualTo(List.of("green")),
								subEntry -> assertThat(subEntry.subEntries().get("a"))
										.asInstanceOf(type(ConfigEntry.class)).satisfies(
												entry -> assertThat(attributesExtension.getAttributeValues(entry, "color"))
														.isEqualTo(List.of("red"))
										),
								subEntry -> assertThat(subEntry.subEntries().get("b"))
										.asInstanceOf(type(ConfigEntry.class)).satisfies(
												entry -> assertThat(attributesExtension.getAttributeValues(entry, "color"))
														.isNullOrEmpty()
										)
						),
				compoundEntry -> assertThat(compoundEntry.subEntries().get("sub2"))
						.asInstanceOf(type(CompoundConfigEntry.class)).satisfies(
								subEntry -> assertThat(attributesExtension.getAttributeValues(subEntry, "color"))
										.isEqualTo(List.of("green")),
								subEntry -> assertThat(attributesExtension.getAttributeValues(subEntry, "scope"))
										.isEqualTo(List.of("world")),
								subEntry -> assertThat(subEntry.subEntries().get("a"))
										.asInstanceOf(type(ConfigEntry.class)).satisfies(
												entry -> assertThat(attributesExtension.getAttributeValues(entry, "color"))
														.isEqualTo(List.of("red")),
												entry -> assertThat(attributesExtension.getAttributeValues(entry, "scope"))
														.isEqualTo(List.of("game"))
										),
								subEntry -> assertThat(subEntry.subEntries().get("b"))
										.asInstanceOf(type(ConfigEntry.class)).satisfies(
												entry -> assertThat(attributesExtension.getAttributeValues(entry, "color"))
														.isEqualTo(List.of("green")),
												entry -> assertThat(attributesExtension.getAttributeValues(entry, "scope"))
														.isEqualTo(List.of("game"))
										)
						)
		);
	}

	@PojoWeaving
	@TweedExtension(AttributesExtension.class)
	@DefaultWeavingExtensions
	@PojoWeavingExtension(AttributesPojoWeavingProcessor.class)
	@CompoundWeaving
	public static class Config {
		@Attribute(key = "scope", value = "game")
		public String string;
		@Attribute(key = "color", value = "green")
		public SubConfig sub1;
		@AttributeDefault(key = "color", defaultValue = "green")
		@AttributeDefault(key = "scope", defaultValue = "game")
		@Attribute(key = "scope", value = "world")
		public SubConfig sub2;
	}

	@CompoundWeaving
	public static class SubConfig {
		@Attribute(key = "color", value = "red")
		public String a;
		public String b;
	}
}
