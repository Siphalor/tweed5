package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.construct.api.TweedConstructFactory;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public interface TweedPojoWeavingExtension extends TweedPojoWeavingFunction {
	TweedConstructFactory<TweedPojoWeavingExtension> FACTORY =
			TweedConstructFactory.builder(TweedPojoWeavingExtension.class)
					.typedArg(ConfigContainer.class)
					.build();

	@ApiStatus.OverrideOnly
	void setup(SetupContext context);

	default <T> void beforeWeaveEntry(ActualType<T> valueType, Patchwork extensionsData, ProtoWeavingContext context) {}

	@Override
	default @Nullable <T> ConfigEntry<T> weaveEntry(ActualType<T> valueType, WeavingContext context) {
		return null;
	}

	default <T> void afterWeaveEntry(ActualType<T> valueType, ConfigEntry<T> configEntry, WeavingContext context) {}

	default <T> void afterWeave() {}

	interface SetupContext {
		<E> PatchworkPartAccess<E> registerWeavingContextExtensionData(Class<E> dataClass);
	}
}
