package de.siphalor.tweed5.weaver.pojo.api.entry;

import de.siphalor.tweed5.construct.api.TweedConstructFactory;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * {@inheritDoc}
 * <br />
 * A constructor taking the value {@link Class} and a {@link Supplier} that allows to instantiate the value Class with no arguments.
 */
public interface WeavableCompoundConfigEntry<T> extends CompoundConfigEntry<T> {
	@SuppressWarnings("rawtypes")
	TweedConstructFactory<WeavableCompoundConfigEntry> FACTORY =
			TweedConstructFactory.builder(WeavableCompoundConfigEntry.class)
					.typedArg(ConfigContainer.class)
					.typedArg(Class.class) // the value class
					.typedArg(Supplier.class) // constructor for the value class
					.namedArg("subEntries", List.class) // List of SubEntry's
					.build();

	@Value
	@RequiredArgsConstructor
	class SubEntry {
		String name;
		ConfigEntry<?> configEntry;
		@Nullable MethodHandle getter;
		@Nullable MethodHandle setter;
	}
}
