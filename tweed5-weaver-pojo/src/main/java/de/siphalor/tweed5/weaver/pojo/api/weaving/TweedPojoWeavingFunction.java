package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface TweedPojoWeavingFunction {
	/**
	 * Weaves a {@link ConfigEntry} for the given value class and context.
	 * The returned config entry must be sealed.
	 * @return The resulting, sealed config entry or {@code null}, if the weaving function is not applicable to the given parameters.
	 */
	<T> @Nullable ConfigEntry<T> weaveEntry(ActualType<T> valueType, WeavingContext context);

	@FunctionalInterface
	interface NonNull extends TweedPojoWeavingFunction {
		/**
		 * {@inheritDoc}
		 * <br />
		 * The function must ensure that the resulting entry is not null, e.g., by trowing a {@link RuntimeException}.
		 * @return The resulting, sealed config entry.
		 * @throws RuntimeException when a valid config entry could not be resolved.
		 */
		@Override
		<T> @org.jspecify.annotations.NonNull ConfigEntry<T> weaveEntry(ActualType<T> valueType, WeavingContext context);
	}
}
