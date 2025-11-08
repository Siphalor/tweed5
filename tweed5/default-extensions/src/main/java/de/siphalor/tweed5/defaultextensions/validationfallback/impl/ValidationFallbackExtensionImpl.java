package de.siphalor.tweed5.defaultextensions.validationfallback.impl;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.ConfigEntryValidator;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationProvidingExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssue;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import de.siphalor.tweed5.defaultextensions.validationfallback.api.ValidationFallbackExtension;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationFallbackExtensionImpl implements ValidationFallbackExtension, ValidationProvidingExtension {
	private final ConfigContainer<?> configContainer;
	private final PatchworkPartAccess<Boolean> hasToStringAccess;
	private @Nullable PresetsExtension presetsExtension;

	private String fallbackPresetName = PresetsExtension.DEFAULT_PRESET_NAME;

	public ValidationFallbackExtensionImpl(TweedExtensionSetupContext context, ConfigContainer<?> configContainer) {
		this.configContainer = configContainer;
		this.hasToStringAccess = context.registerEntryExtensionData(Boolean.class);
		context.registerExtension(PresetsExtension.class);
	}

	private PresetsExtension getOrResolvePresetsExtension() {
		if (presetsExtension == null) {
			presetsExtension = configContainer.extension(PresetsExtension.class)
					.orElseThrow(() -> new IllegalStateException("No presets extension registered"));
		}
		return presetsExtension;
	}

	@Override
	public void initEntry(ConfigEntry<?> configEntry) {
		configEntry.extensionsData().set(hasToStringAccess, hasCustomToString(configEntry.valueClass()));
	}

	private boolean hasCustomToString(Class<?> clazz) {
		if (clazz.isPrimitive()) {
			return true;
		}
		try {
			return clazz.getMethod("toString").getDeclaringClass() != Object.class;
		} catch (Exception ignored) {
			return false;
		}
	}

	@Override
	public void fallbackToPreset(String presetName) {
		this.fallbackPresetName = presetName;
	}

	@Override
	public Middleware<ConfigEntryValidator> validationMiddleware() {
		return new ValidationFallbackMiddleware();
	}

	private class ValidationFallbackMiddleware implements Middleware<ConfigEntryValidator> {
		@Override
		public String id() {
			return EXTENSION_ID;
		}

		@Override
		public Set<String> mustComeBefore() {
			return Collections.emptySet();
		}

		@Override
		public Set<String> mustComeAfter() {
			return Collections.singleton("$default.end");
		}

		@Override
		public ConfigEntryValidator process(ConfigEntryValidator inner) {
			return new ConfigEntryValidator() {
				@Override
				public <T extends @Nullable Object> ValidationResult<T> validate(ConfigEntry<T> configEntry, T value) {
					ValidationResult<T> result = inner.validate(configEntry, value);
					if (!result.hasError()) {
						return result;
					}
					PresetsExtension presetsExtension = getOrResolvePresetsExtension();

					T fallbackValue = presetsExtension.presetValue(configEntry, fallbackPresetName);
					if (fallbackValue != null) {
						if (configEntry.valueClass().isInstance(fallbackValue)) {
							fallbackValue = configEntry.deepCopy(fallbackValue);
						} else {
							ArrayList<ValidationIssue> issues = new ArrayList<>(result.issues());
							issues.add(new ValidationIssue(
									"Fallback value is not of correct class, expected " + configEntry.valueClass().getName() +
											", but got " + fallbackValue.getClass().getName(),
									ValidationIssueLevel.ERROR
							));
							return ValidationResult.withIssues(value, issues);
						}
					}

					return ValidationResult.withIssues(
							fallbackValue,
							result.issues().stream()
									.map(issue -> new ValidationIssue(
											issue.message(),
											issue.level() == ValidationIssueLevel.ERROR
													? ValidationIssueLevel.WARN
													: issue.level()
									))
									.collect(Collectors.toList())
					);
				}

				@Override
				public <T> String description(ConfigEntry<T> configEntry) {
					if (Boolean.TRUE.equals(configEntry.extensionsData().get(hasToStringAccess))) {
						T fallbackValue = getOrResolvePresetsExtension().presetValue(configEntry, fallbackPresetName);
						return inner.description(configEntry) + "\n\nDefault/Fallback value: " + fallbackValue;
					}
					return inner.description(configEntry);
				}
			};
		}
	}
}
