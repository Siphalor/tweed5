package de.siphalor.tweed5.core.impl;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.container.ConfigContainerSetupPhase;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkFactory;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import de.siphalor.tweed5.utils.api.collection.InheritanceMap;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class DefaultConfigContainer<T> implements ConfigContainer<T> {
	@Getter
	private ConfigContainerSetupPhase setupPhase = ConfigContainerSetupPhase.EXTENSIONS_SETUP;
	private final Set<Class<? extends TweedExtension>> requestedExtensions = new HashSet<>();
	private final InheritanceMap<TweedExtension> extensions = new InheritanceMap<>(TweedExtension.class);
	private @Nullable ConfigEntry<T> rootEntry;
	private @Nullable PatchworkFactory entryExtensionsDataPatchworkFactory;

	@Override
	public void registerExtension(Class<? extends TweedExtension> extensionClass) {
		requireSetupPhase(ConfigContainerSetupPhase.EXTENSIONS_SETUP);
		requestedExtensions.add(extensionClass);
	}

	@Override
	public void finishExtensionSetup() {
		requireSetupPhase(ConfigContainerSetupPhase.EXTENSIONS_SETUP);

		PatchworkFactory.Builder entryExtensionDataPatchworkFactoryBuilder = PatchworkFactory.builder();

		TweedExtensionSetupContext extensionSetupContext = new TweedExtensionSetupContext() {
			@Override
			public <E> PatchworkPartAccess<E> registerEntryExtensionData(Class<E> dataClass) {
				return entryExtensionDataPatchworkFactoryBuilder.registerPart(dataClass);
			}

			@Override
			public void registerExtension(Class<? extends TweedExtension> extensionClass) {
				if (!extensions.containsAnyInstanceForClass(extensionClass)) {
					requestedExtensions.add(extensionClass);
				}
			}
		};

		Set<Class<? extends TweedExtension>> abstractExtensionClasses = new HashSet<>();

		while (true) {
			if (!requestedExtensions.isEmpty()) {
				Class<? extends TweedExtension> extensionClass = popFromIterable(requestedExtensions);
				if (isAbstractClass(extensionClass)) {
					abstractExtensionClasses.add(extensionClass);
				} else {
					extensions.put(instantiateExtension(extensionClass, extensionSetupContext));
				}
			} else if (!abstractExtensionClasses.isEmpty()) {
				Class<? extends TweedExtension> extensionClass = popFromIterable(abstractExtensionClasses);
				if (!extensions.containsAnyInstanceForClass(extensionClass)) {
					extensions.put(instantiateAbstractExtension(extensionClass, extensionSetupContext));
				}
			} else {
				break;
			}
		}

		entryExtensionsDataPatchworkFactory = entryExtensionDataPatchworkFactoryBuilder.build();

		setupPhase = ConfigContainerSetupPhase.TREE_SETUP;

		extensions.values().forEach(TweedExtension::extensionsFinalized);
	}

	private static <T> T popFromIterable(Iterable<T> iterable) {
		Iterator<T> iterator = iterable.iterator();
		T value = iterator.next();
		iterator.remove();
		return value;
	}

	protected <E extends TweedExtension> E instantiateAbstractExtension(
			Class<E> extensionClass,
			TweedExtensionSetupContext setupContext
	) {
		try {
			Field defaultField = extensionClass.getDeclaredField("DEFAULT");
			if (defaultField.getType() != Class.class) {
				throw new IllegalStateException(createAbstractExtensionInstantiationExceptionMessage(
						extensionClass,
						"DEFAULT field has incorrect class " + defaultField.getType().getName() + "."
				));
			}
			if ((defaultField.getModifiers() & Modifier.STATIC) == 0) {
				throw new IllegalStateException(createAbstractExtensionInstantiationExceptionMessage(
						extensionClass,
						"DEFAULT field is not static."
				));
			}
			if ((defaultField.getModifiers() & Modifier.PUBLIC) == 0) {
				throw new IllegalStateException(createAbstractExtensionInstantiationExceptionMessage(
						extensionClass,
						"DEFAULT field is not public."
				));
			}
			Class<?> defaultClass = (Class<?>) defaultField.get(null);
			if (!extensionClass.isAssignableFrom(defaultClass)) {
				throw new IllegalStateException(createAbstractExtensionInstantiationExceptionMessage(
						extensionClass,
						"DEFAULT field contains class " + defaultClass.getName() + ", but that class "
								+ "does not inherit from " + extensionClass.getName()
				));
			}
			//noinspection unchecked
			return instantiateExtension((Class<? extends E>) defaultClass, setupContext);
		} catch (NoSuchFieldException ignored) {
			throw new IllegalStateException(createAbstractExtensionInstantiationExceptionMessage(extensionClass, null));
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(
					createAbstractExtensionInstantiationExceptionMessage(
							extensionClass,
							"Couldn't access DEFAULT field."
					),
					e
			);
		}
	}

	private String createAbstractExtensionInstantiationExceptionMessage(
			Class<?> extensionClass,
			@Nullable String detail
	) {
		StringBuilder sb = new StringBuilder();
		sb.append("Requested extension class ").append(extensionClass.getName()).append(" is ");
		if (extensionClass.isInterface()) {
			sb.append("an interface ");
		} else {
			sb.append("an abstract class ");
		}
		sb.append("and cannot be instantiated directly.\n");
		sb.append("As the extension developer you can declare a public static DEFAULT field containing the class of ");
		sb.append("a default implementation.\n");
		sb.append("As a user you can try registering an implementation of the extension class directly.");
		if (detail != null) {
			sb.append("\n");
			sb.append(detail);
		}
		return sb.toString();
	}

	protected  <E extends TweedExtension> E instantiateExtension(
			Class<E> extensionClass,
			TweedExtensionSetupContext setupContext
	) {
		if (isAbstractClass(extensionClass)) {
			throw new IllegalStateException(
					"Cannot instantiate extension class " + extensionClass.getName() + " as it is abstract"
			);
		}
		return TweedExtension.FACTORY.construct(extensionClass)
				.typedArg(TweedExtensionSetupContext.class, setupContext)
				.typedArg(ConfigContainer.class, this)
				.finish();
	}

	private boolean isAbstractClass(Class<?> clazz) {
		return clazz.isInterface() || (clazz.getModifiers() & Modifier.ABSTRACT) != 0;
	}

	@Override
	public <E extends TweedExtension> Optional<E> extension(Class<E> extensionClass) {
		requireSetupPhase(
				ConfigContainerSetupPhase.TREE_SETUP,
				ConfigContainerSetupPhase.TREE_ATTACHED,
				ConfigContainerSetupPhase.INITIALIZED
		);
		try {
			return Optional.ofNullable(extensions.getSingleInstance(extensionClass));
		} catch (InheritanceMap.NonUniqueResultException e) {
			throw new IllegalStateException("Multiple extensions registered for class " + extensionClass.getName(), e);
		}
	}

	@Override
	public Collection<TweedExtension> extensions() {
		requireSetupPhase(
				ConfigContainerSetupPhase.TREE_SETUP,
				ConfigContainerSetupPhase.TREE_ATTACHED,
				ConfigContainerSetupPhase.INITIALIZED
		);
		return Collections.unmodifiableCollection(extensions.values());
	}

	@Override
	public void attachTree(ConfigEntry<T> rootEntry) {
		requireSetupPhase(ConfigContainerSetupPhase.TREE_SETUP);

		this.rootEntry = rootEntry;

		setupPhase = ConfigContainerSetupPhase.TREE_ATTACHED;
	}

	@Override
	public Patchwork createExtensionsData() {
		requireSetupPhase(ConfigContainerSetupPhase.TREE_SETUP);

		assert entryExtensionsDataPatchworkFactory != null;
		return entryExtensionsDataPatchworkFactory.create();
	}

	@Override
	public void initialize() {
		requireSetupPhase(ConfigContainerSetupPhase.TREE_ATTACHED);

		for (TweedExtension extension : extensions()) {
			extension.initialize();
		}

		assert rootEntry != null;
		rootEntry.visitInOrder(entry -> {
			for (TweedExtension extension : extensions()) {
				extension.initEntry(entry);
			}
		});

		setupPhase = ConfigContainerSetupPhase.INITIALIZED;
	}

	@Override
	public ConfigEntry<T> rootEntry() {
		requireSetupPhase(ConfigContainerSetupPhase.TREE_ATTACHED, ConfigContainerSetupPhase.INITIALIZED);

		assert rootEntry != null;
		return rootEntry;
	}

	private void requireSetupPhase(ConfigContainerSetupPhase... allowedPhases) {
		for (ConfigContainerSetupPhase allowedPhase : allowedPhases) {
			if (allowedPhase == setupPhase) {
				return;
			}
		}
		if (allowedPhases.length == 1) {
			throw new IllegalStateException(
					"Config container is not in correct phase, expected "
							+ allowedPhases[0]
							+ ", but is in "
							+ setupPhase
			);
		} else {
			throw new IllegalStateException(
					"Config container is not in correct phase, expected any of "
							+ Arrays.toString(allowedPhases)
							+ ", but is in "
							+ setupPhase
			);
		}
	}
}
