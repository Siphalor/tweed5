package de.siphalor.tweed5.weaver.pojo.test;

import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.SimpleConfigEntry;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

public class ConfigEntryAssertions {
	public static Consumer<Object> isSimpleEntryForClass(Class<?> valueClass) {
		return object -> assertThat(object)
				.as("Should be a simple config entry for class  " + valueClass.getName())
				.asInstanceOf(type(SimpleConfigEntry.class))
				.extracting(ConfigEntry::valueClass)
				.isEqualTo(valueClass);
	}

	public static <T> Consumer<Object> isCompoundEntryForClassWith(
			Class<T> compoundClass,
			Consumer<CompoundConfigEntry<T>> condition
	) {
		return object -> assertThat(object)
				.as("Should be a compound config entry for class " + compoundClass.getSimpleName())
				.asInstanceOf(type(CompoundConfigEntry.class))
				.as("Compound entry for class " + compoundClass.getSimpleName())
				.satisfies(
						compoundEntry -> assertThat(compoundEntry.valueClass())
								.as("Value class of compound entry should match")
								.isEqualTo(compoundClass),
						condition::accept
				);
	}
}
