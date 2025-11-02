package de.siphalor.tweed5.weaver.pojoext.presets.api;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import de.siphalor.tweed5.weaver.pojo.api.annotation.*;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.TweedPojoWeaverBootstrapper;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import static de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension.presetValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class PresetsWeavingProcessorTest {

	@Test
	void test() {
		ConfigContainer<Config> configContainer = TweedPojoWeaverBootstrapper.create(Config.class).weave();
		configContainer.initialize();
		PresetsExtension presetsExtension = configContainer.extension(PresetsExtension.class).orElseThrow();

		Config defaultPreset =
				presetsExtension.presetValue(configContainer.rootEntry(), PresetsExtension.DEFAULT_PRESET_NAME);
		Config presetA = presetsExtension.presetValue(configContainer.rootEntry(), "a");
		Config presetB = presetsExtension.presetValue(configContainer.rootEntry(), "b");

		assertThat(defaultPreset).isEqualTo(new Config());
		assertThat(presetA).isEqualTo(Config.PRESET_A);
		assertThat(presetB).isEqualTo(Config.PRESET_B);

		assertThat(configContainer.rootEntry()).asInstanceOf(type(CompoundConfigEntry.class)).satisfies(
				compound -> assertThat(compound.subEntries().get("subConfig"))
						.asInstanceOf(type(ConfigEntry.class))
						.satisfies(
								entry -> assertThat(entry.call(presetValue(PresetsExtension.DEFAULT_PRESET_NAME)))
										.isEqualTo(new SubConfig("DEFAULT")),
								entry -> assertThat(entry.call(presetValue("a"))).isEqualTo(new SubConfig("AAA")),
								entry -> assertThat(entry.call(presetValue("b"))).isEqualTo(new SubConfig("BBB")),
								entry -> assertThat(entry.call(presetValue("special"))).isEqualTo(SubConfig.SPECIAL_PRESET)
						)
		);
	}

	@PojoWeaving
	@TweedExtension(PresetsExtension.class)
	@PojoWeavingExtension(DefaultPresetWeavingProcessor.class)
	@PojoWeavingExtension(PresetsWeavingProcessor.class)
	@DefaultWeavingExtensions
	@CompoundWeaving
	// lombok
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode
	@ToString
	public static class Config {
		@Preset("a")
		public static final Config PRESET_A = new Config("a", 1, true, new SubConfig("AAA"));
		@Preset("b")
		public static final Config PRESET_B = new Config("b", 2, false, new SubConfig("BBB"));

		public String string = "default";
		public int integer = 1234;
		public boolean bool;
		public SubConfig subConfig = new SubConfig("DEFAULT");
	}

	@CompoundWeaving
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode
	@ToString
	public static class SubConfig {
		@Preset("special")
		public static final SubConfig SPECIAL_PRESET = new SubConfig("SPECIAL");
		public String value;
	}
}
