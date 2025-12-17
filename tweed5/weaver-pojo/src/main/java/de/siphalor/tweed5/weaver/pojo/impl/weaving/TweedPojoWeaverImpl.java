package de.siphalor.tweed5.weaver.pojo.impl.weaving;

import de.siphalor.tweed5.annotationinheritance.api.AnnotationInheritanceAwareAnnotatedElement;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.container.ConfigContainerSetupPhase;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkFactory;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.weaver.pojo.api.TweedPojoWeaver;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.api.annotation.TweedExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.ProtoWeavingContext;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.WeavingContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class that sets up and handles all the bits and bobs for weaving a {@link ConfigContainer} out of a POJO.
 * The POJO must be annotated with {@link PojoWeaving}.
 */
@CommonsLog
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TweedPojoWeaverImpl<T> implements TweedPojoWeaver<T> {
	@Getter
	private final Class<T> pojoClass;
	private final AnnotatedElement pojoAnnotations;
	private final PojoWeaving rootWeavingConfig;

	private final Set<Class<? extends TweedPojoWeavingExtension>> weavingExtensionClasses = new LinkedHashSet<>();
	private @Nullable Collection<TweedPojoWeavingExtension> weavingExtensions;

	private @Nullable ConfigContainer<T> configContainer;
	private final Set<Class<? extends de.siphalor.tweed5.core.api.extension.TweedExtension>> extensionClasses = new LinkedHashSet<>();

	private final WeavingContext.WeavingFn weavingContextFn = new WeavingContext.WeavingFn() {
		@Override
		public <U> ConfigEntry<U> weaveEntry(
				ActualType<U> valueType,
				Patchwork extensionsData,
				ProtoWeavingContext context
		) {
			return TweedPojoWeaverImpl.this.weaveEntry(valueType, extensionsData, context);
		}

		@Override
		public <U> ConfigEntry<U> weavePseudoEntry(WeavingContext parentContext, String pseudoEntryName, Patchwork extensionsData) {
			return TweedPojoWeaverImpl.this.weavePseudoEntry(parentContext, pseudoEntryName, extensionsData);
		}
	};

	private @Nullable PatchworkFactory weavingExtensionsPatchworkFactory;

	public static <T> TweedPojoWeaverImpl<T> implForClass(Class<T> pojoClass) {
		AnnotatedElement pojoAnnotations = new AnnotationInheritanceAwareAnnotatedElement(pojoClass);
		PojoWeaving rootWeavingConfig = expectAnnotation(pojoAnnotations, PojoWeaving.class);
		return new TweedPojoWeaverImpl<>(pojoClass, pojoAnnotations, rootWeavingConfig);
	}

	private static <A extends Annotation> A expectAnnotation(AnnotatedElement element, Class<A> annotationClass) {
		A annotation = element.getAnnotation(annotationClass);
		if (annotation == null) {
			throw new PojoWeavingException("Annotation "
					+ annotationClass.getName()
					+ " must be defined on class "
					+ element);
		} else {
			return annotation;
		}
	}

	@Override
	public TweedPojoWeaver<T> withConfigContainer(ConfigContainer<T> container) {
		if (configContainer != null) {
			throw new IllegalStateException("Config container already set");
		}
		this.configContainer = container;
		return this;
	}

	@Override
	public ConfigContainer<T> configContainer() {
		if (configContainer != null) {
			return configContainer;
		}

		//noinspection unchecked
		this.configContainer
				= (ConfigContainer<T>) createConfigContainer((Class<? extends ConfigContainer<?>>) rootWeavingConfig.container());

		for (TweedExtension annotation : pojoAnnotations.getAnnotationsByType(TweedExtension.class)) {
			extensionClasses.add(annotation.value());
		}

		//noinspection unchecked
		configContainer.registerExtensions(extensionClasses.toArray(new Class[0]));

		return configContainer;
	}

	private static ConfigContainer<?> createConfigContainer(Class<? extends ConfigContainer<?>> containerClass) {
		try {
			return ConfigContainer.FACTORY.construct(containerClass).finish();
		} catch (Exception e) {
			throw new PojoWeavingException("Failed to instantiate config container");
		}
	}

	@Override
	public TweedPojoWeaver<T> withWeavingExtension(Class<? extends TweedPojoWeavingExtension> weavingExtension) {
		if (configContainer != null
				&& configContainer.setupPhase().compareTo(ConfigContainerSetupPhase.TREE_ATTACHED) >= 0) {
			throw new IllegalStateException("Cannot add weaving extensions after the tree has been attached");
		}

		weavingExtensionClasses.add(weavingExtension);

		return this;
	}

	@Override
	public TweedPojoWeaver<T> withExtension(Class<? extends de.siphalor.tweed5.core.api.extension.TweedExtension> extension) {
		if (configContainer != null
				&& configContainer.setupPhase().compareTo(ConfigContainerSetupPhase.EXTENSIONS_SETUP) > 0) {
			throw new IllegalStateException("Cannot add extensions after the extensions have been finalized");
		}

		extensionClasses.add(extension);

		if (configContainer != null) {
			configContainer.registerExtension(extension);
		}

		return this;
	}

	public ConfigContainer<T> weave() {
		ConfigContainer<T> configContainer = configContainer();

		if (configContainer.setupPhase().compareTo(ConfigContainerSetupPhase.TREE_ATTACHED) >= 0) {
			throw new IllegalStateException("Cannot weave twice");
		}
		if (configContainer.setupPhase() == ConfigContainerSetupPhase.EXTENSIONS_SETUP) {
			configContainer.finishExtensionSetup();
		}

		ConfigEntry<T> rootEntry = this.weaveEntry(
				ActualType.ofClass(pojoClass),
				weavingExtensionsPatchworkFactory().create(),
				ProtoWeavingContext.create(configContainer, pojoAnnotations)
		);

		configContainer.attachTree(rootEntry);

		runAfterWeaveHooks();

		return configContainer;
	}

	private PatchworkFactory weavingExtensionsPatchworkFactory() {
		if (weavingExtensionsPatchworkFactory != null) {
			return weavingExtensionsPatchworkFactory;
		}

		PatchworkFactory.Builder weavingExtensionsPatchworkFactoryBuilder = PatchworkFactory.builder();

		TweedPojoWeavingExtension.SetupContext setupContext = weavingExtensionsPatchworkFactoryBuilder::registerPart;

		for (TweedPojoWeavingExtension weavingExtension : weavingExtensions()) {
			weavingExtension.setup(setupContext);
		}

		weavingExtensionsPatchworkFactory = weavingExtensionsPatchworkFactoryBuilder.build();

		return weavingExtensionsPatchworkFactory;
	}

	private Collection<TweedPojoWeavingExtension> weavingExtensions() {
		if (weavingExtensions != null) {
			return weavingExtensions;
		}

		for (PojoWeavingExtension annotation : pojoAnnotations.getAnnotationsByType(PojoWeavingExtension.class)) {
			weavingExtensionClasses.add(annotation.value());
		}

		weavingExtensions = weavingExtensionClasses.stream()
				.map(weavingExtensionClass ->
						TweedPojoWeavingExtension.FACTORY.construct(weavingExtensionClass)
								.typedArg(ConfigContainer.class, configContainer)
								.finish()
				)
				.collect(Collectors.toList());
		return weavingExtensions;
	}

	private <U> ConfigEntry<U> weaveEntry(
			ActualType<U> valueType,
			Patchwork extensionsData,
			ProtoWeavingContext protoContext
	) {
		assert configContainer != null && weavingExtensions != null;

		extensionsData = extensionsData.copy();

		runBeforeWeaveEntryHooks(valueType, extensionsData, protoContext);

		WeavingContext context = WeavingContext.builder()
				.parent(protoContext.parent())
				.weavingFunction(weavingContextFn)
				.configContainer(configContainer)
				.valueType(valueType)
				.path(protoContext.path())
				.extensionsData(extensionsData)
				.annotations(new AnnotationInheritanceAwareAnnotatedElement(protoContext.annotations()))
				.build();

		for (TweedPojoWeavingExtension weavingExtension : weavingExtensions) {
			try {
				ConfigEntry<U> configEntry = weavingExtension.weaveEntry(valueType, context);
				if (configEntry != null) {
					runAfterWeaveEntryHooks(valueType, configEntry, context);
					return configEntry;
				}
			} catch (Exception e) {
				log.error(
						"Failed to run Tweed POJO weaver (" + weavingExtension.getClass().getName() + ")"
								+ " for entry at " + Arrays.toString(context.path()),
						e
				);
			}
		}

		throw new PojoWeavingException(
				"Failed to weave entry for " + valueType + " at " + Arrays.toString(context.path())
						+ ": No matching weavers found.\n"
						+ "Registered weaving extensions: "
						+ weavingExtensions.stream()
						.map(TweedPojoWeavingExtension::getClass)
						.map(Class::getName)
						.collect(Collectors.joining(", "))
		);
	}

	private <U> ConfigEntry<U> weavePseudoEntry(
			WeavingContext parentContext,
			String pseudoEntryName,
			Patchwork extensionsData
	) {
		assert configContainer != null && weavingExtensions != null;

		extensionsData = extensionsData.copy();

		//noinspection unchecked
		ActualType<U> valueType = (ActualType<U>) parentContext.valueType();

		String[] path = Arrays.copyOf(parentContext.path(), parentContext.path().length + 1);
		path[path.length - 1] = ":" + pseudoEntryName;

		WeavingContext context = WeavingContext.builder()
				.parent(parentContext)
				.weavingFunction(weavingContextFn)
				.configContainer(configContainer)
				.path(path)
				.valueType(parentContext.valueType())
				.isPseudo(true)
				.extensionsData(extensionsData)
				.annotations(parentContext.annotations())
				.build();

		for (TweedPojoWeavingExtension weavingExtension : weavingExtensions) {
			try {
				ConfigEntry<U> configEntry = weavingExtension.weaveEntry(valueType, context);
				if (configEntry != null) {
					runAfterWeaveEntryHooks(valueType, configEntry, context);
					return configEntry;
				}
			} catch (Exception e) {
				log.error(
						"Failed to run Tweed POJO weaver (" + weavingExtension.getClass().getName() + ")"
								+ " for pseudo entry at " + Arrays.toString(context.path()),
						e
				);
			}
		}

		throw new PojoWeavingException(
				"Failed to weave pseudo entry for " + valueType + " at " + Arrays.toString(context.path())
						+ ": No matching weavers found"
		);
	}

	private <U> void runBeforeWeaveEntryHooks(
			ActualType<U> dataClass,
			Patchwork extensionsData,
			ProtoWeavingContext protoContext
	) {
		assert weavingExtensions != null;

		for (TweedPojoWeavingExtension weavingExtension : weavingExtensions) {
			try {
				weavingExtension.beforeWeaveEntry(dataClass, extensionsData, protoContext);
			} catch (Exception e) {
				log.error(
						"Failed to apply Tweed POJO weaver before weave hook ("
								+ weavingExtension.getClass().getName() + ")",
						e
				);
			}
		}
	}

	private <U> void runAfterWeaveEntryHooks(ActualType<U> dataClass, ConfigEntry<U> configEntry, WeavingContext context) {
		assert weavingExtensions != null;

		for (TweedPojoWeavingExtension weavingExtension : weavingExtensions) {
			try {
				weavingExtension.afterWeaveEntry(dataClass, configEntry, context);
			} catch (Exception e) {
				log.error(
						"Failed to apply Tweed POJO weaver after weave hook ("
								+ weavingExtension.getClass().getName() + ")",
						e
				);
			}
		}
	}

	private void runAfterWeaveHooks() {
		assert weavingExtensions != null;

		for (TweedPojoWeavingExtension weavingExtension : weavingExtensions) {
			weavingExtension.afterWeave();
		}
	}
}
