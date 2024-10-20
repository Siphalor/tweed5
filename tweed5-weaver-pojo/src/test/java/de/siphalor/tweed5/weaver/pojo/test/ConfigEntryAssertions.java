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
				.asInstanceOf(type(SimpleConfigEntry.class))
				.extracting(ConfigEntry::valueClass)
				.isEqualTo(valueClass);
	}

	@SuppressWarnings("unchecked")
	public static <T> Consumer<Object> isCompoundEntryForClassWith(
			Class<T> compoundClass,
			Consumer<CompoundConfigEntry<T>> condition
	) {
		return object -> assertThat(object)
				.asInstanceOf(type(CompoundConfigEntry.class))
				.satisfies(compoundEntry -> {
					assertThat(compoundEntry.valueClass()).isEqualTo(compoundClass);
					condition.accept(compoundEntry);
				});
	}
}
