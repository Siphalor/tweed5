package de.siphalor.tweed5.attributesextension.impl;

import de.siphalor.tweed5.attributesextension.api.AttributesExtension;
import de.siphalor.tweed5.attributesextension.api.AttributesRelatedExtension;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import de.siphalor.tweed5.utils.api.collection.ImmutableArrayBackedMap;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class AttributesExtensionImpl implements AttributesExtension {
	private final ConfigContainer<?> configContainer;
	private final PatchworkPartAccess<CustomEntryData> dataAccess;
	private boolean initialized;

	public AttributesExtensionImpl(ConfigContainer<?> configContainer, TweedExtensionSetupContext setupContext) {
		this.configContainer = configContainer;
		this.dataAccess = setupContext.registerEntryExtensionData(CustomEntryData.class);
	}

	@Override
	public void setAttribute(ConfigEntry<?> entry, String key, List<String> values) {
		requireEditable();

		Map<String, List<String>> attributes = getOrCreateEditableAttributes(entry);

		attributes.compute(key, (k, existingValues) -> {
			if (existingValues == null) {
				return new ArrayList<>(values);
			} else {
				existingValues.addAll(values);
				return existingValues;
			}
		});
	}

	@Override
	public void setAttributeDefault(ConfigEntry<?> entry, String key, List<String> values) {
		requireEditable();

		Map<String, List<String>> attributeDefaults = getOrCreateEditableAttributeDefaults(entry);

		attributeDefaults.compute(key, (k, existingValues) -> {
			if (existingValues == null) {
				return new ArrayList<>(values);
			} else {
				existingValues.addAll(values);
				return existingValues;
			}
		});
	}

	private void requireEditable() {
		if (initialized) {
			throw new IllegalStateException("Attributes are only editable until the config has been initialized");
		}
	}

	private Map<String, List<String>> getOrCreateEditableAttributes(ConfigEntry<?> entry) {
		CustomEntryData data = getOrCreateCustomEntryData(entry);
		Map<String, List<String>> attributes = data.attributes();
		if (attributes == null) {
			attributes = new HashMap<>();
			data.attributes(attributes);
		}
		return attributes;
	}

	private Map<String, List<String>> getOrCreateEditableAttributeDefaults(ConfigEntry<?> entry) {
		CustomEntryData data = getOrCreateCustomEntryData(entry);
		Map<String, List<String>> attributeDefaults = data.attributeDefaults();
		if (attributeDefaults == null) {
			attributeDefaults = new HashMap<>();
			data.attributeDefaults(attributeDefaults);
		}
		return attributeDefaults;
	}

	private CustomEntryData getOrCreateCustomEntryData(ConfigEntry<?> entry) {
		CustomEntryData customEntryData = entry.extensionsData().get(dataAccess);
		if (customEntryData == null) {
			customEntryData = new CustomEntryData();
			entry.extensionsData().set(dataAccess, customEntryData);
		}
		return customEntryData;
	}

	@Override
	public void initialize() {
		configContainer.rootEntry().visitInOrder(new ConfigEntryVisitor() {
			private final Deque<Map<String, List<String>>> defaults = new ArrayDeque<>(
					Collections.singletonList(Collections.emptyMap())
			);

			@Override
			public boolean enterStructuredEntry(ConfigEntry<?> entry) {
				enterEntry(entry);
				return true;
			}

			private void enterEntry(ConfigEntry<?> entry) {
				CustomEntryData data = entry.extensionsData().get(dataAccess);
				Map<String, List<String>> currentDefaults = defaults.getFirst();
				if (data == null) {
					defaults.push(currentDefaults);
					return;
				}
				Map<String, List<String>> entryDefaults = data.attributeDefaults();
				data.attributeDefaults(null);
				if (entryDefaults == null || entryDefaults.isEmpty()) {
					defaults.push(currentDefaults);
					return;
				}

				defaults.push(mergeMapsAndSeal(currentDefaults, entryDefaults));

				visitEntry(entry);
			}

			@Override
			public void leaveStructuredEntry(ConfigEntry<?> entry) {
				defaults.pop();
			}

			@Override
			public void visitEntry(ConfigEntry<?> entry) {
				CustomEntryData data = getOrCreateCustomEntryData(entry);
				Map<String, List<String>> currentDefaults = defaults.getFirst();
				if (data.attributes() == null || data.attributes().isEmpty()) {
					data.attributes(currentDefaults);
				} else {
					data.attributes(mergeMapsAndSeal(currentDefaults, data.attributes()));
				}
			}

			private Map<String, List<String>> mergeMapsAndSeal(
					Map<String, List<String>> base,
					Map<String, List<String>> overrides
			) {
				if (overrides.isEmpty()) {
					return ImmutableArrayBackedMap.ofEntries(base.entrySet());
				} else if (base.isEmpty()) {
					return ImmutableArrayBackedMap.ofEntries(overrides.entrySet());
				}

				List<Map.Entry<String, List<String>>> entries = new ArrayList<>(base.size() + overrides.size());
				overrides.forEach((key, value) ->
						entries.add(new AbstractMap.SimpleEntry<>(key, Collections.unmodifiableList(value)))
				);
				base.forEach((key, value) -> {
					if (!overrides.containsKey(key)) {
						entries.add(new AbstractMap.SimpleEntry<>(key, Collections.unmodifiableList(value)));
					}
				});
				return ImmutableArrayBackedMap.ofEntries(entries);
			}
		});

		initialized = true;

		for (TweedExtension extension : configContainer.extensions()) {
			if (extension instanceof AttributesRelatedExtension) {
				((AttributesRelatedExtension) extension).afterAttributesInitialized();
			}
		}
	}

	@Override
	public List<String> getAttributeValues(ConfigEntry<?> entry, String key) {
		requireInitialized();
		CustomEntryData data = entry.extensionsData().get(dataAccess);
		return Optional.ofNullable(data)
				.map(CustomEntryData::attributes)
				.map(attributes -> attributes.get(key))
				.orElse(Collections.emptyList());
	}

	@Override
	public @Nullable String getAttributeValue(ConfigEntry<?> entry, String key) {
		requireInitialized();
		CustomEntryData data = entry.extensionsData().get(dataAccess);
		return Optional.ofNullable(data)
				.map(CustomEntryData::attributes)
				.map(attributes -> attributes.get(key))
				.map(values -> values.isEmpty() ? null : values.get(values.size() - 1))
				.orElse(null);
	}

	private void requireInitialized() {
		if (!initialized) {
			throw new IllegalStateException("Attributes are only available after the config has been initialized");
		}
	}

	@Data
	private static class CustomEntryData {
		private @Nullable Map<String, List<String>> attributes;
		private @Nullable Map<String, List<String>> attributeDefaults;
	}
}
