package de.siphalor.tweed5.coat.bridge.impl;

import de.siphalor.coat.screen.ConfigContentWidget;
import de.siphalor.coat.screen.ConfigScreen;
import de.siphalor.tweed5.coat.bridge.api.ConfigScreenCreateParams;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatBridgeExtension;
import de.siphalor.tweed5.coat.bridge.api.mapping.TweedCoatEntryCreationContext;
import de.siphalor.tweed5.coat.bridge.api.mapping.TweedCoatEntryMappingContext;
import de.siphalor.tweed5.coat.bridge.api.mapping.TweedCoatEntryMappingResult;
import de.siphalor.tweed5.coat.bridge.api.mapping.TweedCoatMapper;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TweedCoatBridgeExtensionImpl implements TweedCoatBridgeExtension {
	private final PatchworkPartAccess<CustomData> customDataAccess;
	private final List<TweedCoatMapper<?>> mappers = new ArrayList<>();

	public TweedCoatBridgeExtensionImpl(TweedExtensionSetupContext setupContext) {
		customDataAccess = setupContext.registerEntryExtensionData(CustomData.class);
	}

	@Override
	public void addMapper(TweedCoatMapper<?> mapper) {
		mappers.add(mapper);
	}

	@Override
	public <T> ConfigScreen createConfigScreen(ConfigScreenCreateParams<T> params) {
		Minecraft minecraft = Minecraft.getInstance();

		TweedCoatEntryMappingContext mappingContext = TweedCoatEntryMappingContext.rootBuilder(
				this::mapEntry,
				params.translationKeyPrefix()
		).parentWidgetClass(ConfigScreen.class).build();

		TweedCoatEntryMappingResult<T, ?> rootResult = mapEntry(params.rootEntry(), mappingContext);
		if (!rootResult.isApplicable()) {
			throw new IllegalStateException("Failed to map root entry");
		}

		T value = params.rootEntry().deepCopy(params.currentValue());
		TweedCoatEntryCreationContext<T> creationContext = TweedCoatEntryCreationContext.<T>builder()
				.entry(params.rootEntry())
				.currentValue(value)
				.defaultValue(params.defaultValue())
				.build();

		ConfigContentWidget contentWidget = rootResult.createContentWidget(creationContext);
		if (contentWidget == null) {
			throw new IllegalStateException("Failed to create root content widget");
		}

		ConfigScreen configScreen = new ConfigScreen(
				minecraft.screen, params.title(), Collections.singletonList(contentWidget)
		);
		configScreen.setOnSave(() -> params.saveHandler().accept(value));
		return configScreen;
	}

	private <T> TweedCoatEntryMappingResult<T, ?> mapEntry(
			ConfigEntry<T> entry,
			TweedCoatEntryMappingContext context
	) {
		for (TweedCoatMapper<?> mapper : mappers) {
			//noinspection rawtypes,unchecked
			TweedCoatEntryMappingResult<T, ?> result = mapper.mapEntry((ConfigEntry) entry, context);
			if (result.isApplicable()) {
				return result;
			}
		}
		return TweedCoatEntryMappingResult.notApplicable();
	}

	private static class CustomData {

	}
}
