package de.siphalor.tweed5.core.impl;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.container.ConfigContainerSetupPhase;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.*;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkClassCreator;
import de.siphalor.tweed5.patchwork.impl.PatchworkClass;
import de.siphalor.tweed5.patchwork.impl.PatchworkClassGenerator;
import de.siphalor.tweed5.patchwork.impl.PatchworkClassPart;
import de.siphalor.tweed5.utils.api.collection.InheritanceMap;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.*;

@NullUnmarked
public class DefaultConfigContainer<T> implements ConfigContainer<T> {
	@Getter
	private ConfigContainerSetupPhase setupPhase = ConfigContainerSetupPhase.EXTENSIONS_SETUP;
	private final InheritanceMap<TweedExtension> extensions = new InheritanceMap<>(TweedExtension.class);
	private ConfigEntry<T> rootEntry;
	private PatchworkClass<EntryExtensionsData> entryExtensionsDataPatchworkClass;
	private Map<Class<?>, RegisteredExtensionDataImpl<EntryExtensionsData, ?>> registeredEntryDataExtensions;

	@Override
	public <E extends TweedExtension> @Nullable E extension(Class<E> extensionClass) {
		try {
			return extensions.getSingleInstance(extensionClass);
		} catch (InheritanceMap.NonUniqueResultException e) {
			return null;
		}
	}

	@Override
	public Collection<TweedExtension> extensions() {
		return Collections.unmodifiableCollection(extensions.values());
	}

	@Override
	public void registerExtension(TweedExtension extension) {
		requireSetupPhase(ConfigContainerSetupPhase.EXTENSIONS_SETUP);

		if (!extensions.putIfAbsent(extension)) {
			throw new IllegalArgumentException("Extension " + extension.getClass().getName() + " is already registered");
		}
	}

	@Override
	public void finishExtensionSetup() {
		requireSetupPhase(ConfigContainerSetupPhase.EXTENSIONS_SETUP);
		registeredEntryDataExtensions = new HashMap<>();

		Collection<TweedExtension> additionalExtensions = new ArrayList<>();
		TweedExtensionSetupContext extensionSetupContext = new TweedExtensionSetupContext() {
			@Override
			public ConfigContainer<T> configContainer() {
				return DefaultConfigContainer.this;
			}

			@Override
			public <E> RegisteredExtensionData<EntryExtensionsData, E> registerEntryExtensionData(Class<E> dataClass) {
				if (registeredEntryDataExtensions.containsKey(dataClass)) {
					throw new IllegalArgumentException("Extension " + dataClass.getName() + " is already registered");
				}
				RegisteredExtensionDataImpl<EntryExtensionsData, E> registered = new RegisteredExtensionDataImpl<>();
				registeredEntryDataExtensions.put(dataClass, registered);
				return registered;
			}

			@Override
			public void registerExtension(TweedExtension extension) {
				if (!extensions.containsAnyInstanceForClass(extension.getClass())) {
					additionalExtensions.add(extension);
				}
			}
		};

		Collection<TweedExtension> extensionsToSetup = extensions.values();
		while (!extensionsToSetup.isEmpty()) {
			for (TweedExtension extension : extensionsToSetup) {
				extension.setup(extensionSetupContext);
			}

			for (TweedExtension additionalExtension : additionalExtensions) {
				extensions.putIfAbsent(additionalExtension);
			}
			extensionsToSetup = new ArrayList<>(additionalExtensions);
			additionalExtensions.clear();
		}

		PatchworkClassCreator<EntryExtensionsData> entryExtensionsDataGenerator = PatchworkClassCreator.<EntryExtensionsData>builder()
				.patchworkInterface(EntryExtensionsData.class)
				.classPackage("de.siphalor.tweed5.core.generated.entryextensiondata")
				.classPrefix("EntryExtensionsData$")
				.build();
		try {
			entryExtensionsDataPatchworkClass = entryExtensionsDataGenerator.createClass(registeredEntryDataExtensions.keySet());
			for (PatchworkClassPart part : entryExtensionsDataPatchworkClass.parts()) {
				RegisteredExtensionDataImpl<EntryExtensionsData, ?> registeredExtension = registeredEntryDataExtensions.get(part.partInterface());
				registeredExtension.setter(part.fieldSetter());
			}
		} catch (PatchworkClassGenerator.GenerationException e) {
			throw new IllegalStateException("Failed to create patchwork class for entry extensions' data", e);
		}

		setupPhase = ConfigContainerSetupPhase.TREE_SETUP;
	}

	@Override
	public void attachAndSealTree(ConfigEntry<T> rootEntry) {
		requireSetupPhase(ConfigContainerSetupPhase.TREE_SETUP);
		this.rootEntry = rootEntry;
		finishEntrySetup();
	}

	private void finishEntrySetup() {
		rootEntry.visitInOrder(entry -> {
			if (!entry.sealed()) {
				entry.seal(DefaultConfigContainer.this);
			}
		});

		setupPhase = ConfigContainerSetupPhase.TREE_SEALED;
	}

	@Override
	public EntryExtensionsData createExtensionsData() {
		requireSetupPhase(ConfigContainerSetupPhase.TREE_SETUP);

		try {
			return (EntryExtensionsData) entryExtensionsDataPatchworkClass.constructor().invoke();
		} catch (Throwable e) {
			throw new IllegalStateException("Failed to construct patchwork class for entry extensions' data", e);
		}
	}

	@Override
	public Map<Class<?>, ? extends RegisteredExtensionData<EntryExtensionsData, ?>> entryDataExtensions() {
		requireSetupPhase(ConfigContainerSetupPhase.TREE_SEALED);
		return registeredEntryDataExtensions;
	}

	@Override
	public void initialize() {
		requireSetupPhase(ConfigContainerSetupPhase.TREE_SEALED);

		rootEntry.visitInOrder(entry -> {
			for (TweedExtension extension : extensions()) {
				extension.initEntry(entry);
			}
		});

		setupPhase = ConfigContainerSetupPhase.READY;
	}

	@Override
	public ConfigEntry<T> rootEntry() {
		return rootEntry;
	}

	private void requireSetupPhase(ConfigContainerSetupPhase required) {
		if (setupPhase != required) {
			throw new IllegalStateException("Config container is not in correct stage, expected " + required + ", but is in " + setupPhase);
		}
	}

	@Setter
	private static class RegisteredExtensionDataImpl<U extends Patchwork<U>, E> implements RegisteredExtensionData<U, E> {
		private MethodHandle setter;

		@Override
		public void set(U patchwork, E extension) {
			try {
				setter.invokeWithArguments(patchwork, extension);
			} catch (Throwable e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
