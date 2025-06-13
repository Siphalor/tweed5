package de.siphalor.tweed5.defaultextensions.validation.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssue;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssues;
import de.siphalor.tweed5.defaultextensions.validation.api.validators.SimpleValidatorMiddleware;
import de.siphalor.tweed5.defaultextensions.validation.impl.ValidationExtensionImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ValidationExtension extends TweedExtension {
	Class<? extends ValidationExtension> DEFAULT = ValidationExtensionImpl.class;

	static <C extends ConfigEntry<T>, T> Consumer<C> validators(ConfigEntryValidator... validators) {
		return entry -> {
			ValidationExtension extension = entry.container().extension(ValidationExtension.class)
					.orElseThrow(() -> new IllegalStateException("No validation extension registered"));
			extension.addValidators(entry, validators);
		};
	}

	static <C extends ConfigEntry<T>, T> Function<C, ValidationIssues> validate(T value) {
		return entry -> {
			ValidationExtension extension = entry.container().extension(ValidationExtension.class)
					.orElseThrow(() -> new IllegalStateException("No validation extension registered"));
			return extension.validate(entry, value);
		};
	}

	default <T> void addValidators(ConfigEntry<T> entry, ConfigEntryValidator... validators) {
		String lastId = null;
		for (ConfigEntryValidator validator : validators) {
			String id = UUID.randomUUID().toString();
			Set<String> mustComeAfter = lastId == null ? Collections.emptySet() : Collections.singleton(lastId);

			addValidatorMiddleware(entry, new SimpleValidatorMiddleware(id, validator) {
				@Override
				public Set<String> mustComeAfter() {
					return mustComeAfter;
				}
			});

			lastId = id;
		}
	}
	<T> void addValidatorMiddleware(ConfigEntry<T> entry, Middleware<ConfigEntryValidator> validator);

	<T extends @Nullable Object> ValidationIssues validate(ConfigEntry<T> entry, T value);
}
