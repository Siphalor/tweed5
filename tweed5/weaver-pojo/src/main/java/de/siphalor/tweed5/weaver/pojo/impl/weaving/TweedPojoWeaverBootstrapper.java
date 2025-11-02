package de.siphalor.tweed5.weaver.pojo.impl.weaving;

import de.siphalor.tweed5.annotationinheritance.api.AnnotationInheritanceAwareAnnotatedElement;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkFactory;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.api.annotation.TweedExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.ProtoWeavingContext;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.WeavingContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A class that sets up and handles all the bits and bobs for weaving a {@link ConfigContainer} out of a POJO.
 * The POJO must be annotated with {@link PojoWeaving}.
 */
@CommonsLog
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TweedPojoWeaverBootstrapper<T> {
	private final Class<T> pojoClass;
	private final AnnotatedElement pojoAnnotations;
	private final ConfigContainer<T> configContainer;
	private final Collection<TweedPojoWeavingExtension> weavingExtensions;
	private final WeavingContext.WeavingFn weavingContextFn = new WeavingContext.WeavingFn() {
		@Override
		public <U> ConfigEntry<U> weaveEntry(
				ActualType<U> valueType,
				Patchwork extensionsData,
				ProtoWeavingContext context
		) {
			return TweedPojoWeaverBootstrapper.this.weaveEntry(valueType, extensionsData, context);
		}

		@Override
		public <U> ConfigEntry<U> weavePseudoEntry(WeavingContext parentContext, String pseudoEntryName, Patchwork extensionsData) {
			return TweedPojoWeaverBootstrapper.this.weavePseudoEntry(parentContext, pseudoEntryName, extensionsData);
		}
	};
	@Nullable
	private PatchworkFactory weavingExtensionsPatchworkFactory;

	public static <T> TweedPojoWeaverBootstrapper<T> create(Class<T> pojoClass) {
		AnnotatedElement pojoAnnotations = new AnnotationInheritanceAwareAnnotatedElement(pojoClass);
		PojoWeaving rootWeavingConfig = expectAnnotation(pojoAnnotations, PojoWeaving.class);
		PojoWeavingExtension[] extensionAnnotations = pojoAnnotations.getAnnotationsByType(PojoWeavingExtension.class);

		//noinspection unchecked
		ConfigContainer<T>
				configContainer
				= (ConfigContainer<T>) createConfigContainer((Class<? extends ConfigContainer<?>>) rootWeavingConfig.container());

		TweedExtension[] tweedExtensions = pojoAnnotations.getAnnotationsByType(TweedExtension.class);
		//noinspection unchecked
		configContainer.registerExtensions(
				Arrays.stream(tweedExtensions).map(TweedExtension::value).toArray(Class[]::new)
		);
		configContainer.finishExtensionSetup();

		Collection<TweedPojoWeavingExtension> extensions = loadWeavingExtensions(
				Arrays.stream(extensionAnnotations).map(PojoWeavingExtension::value).collect(Collectors.toList()),
				configContainer
		);

		return new TweedPojoWeaverBootstrapper<>(pojoClass, pojoAnnotations, configContainer, extensions);
	}

	private static Collection<TweedPojoWeavingExtension> loadWeavingExtensions(
			Collection<Class<? extends TweedPojoWeavingExtension>> weaverClasses,
			ConfigContainer<?> configContainer
	) {
		return weaverClasses.stream()
				.map(weaverClass ->
						TweedPojoWeavingExtension.FACTORY.construct(weaverClass)
								.typedArg(ConfigContainer.class, configContainer)
								.finish()
				)
				.collect(Collectors.toList());
	}

	private static ConfigContainer<?> createConfigContainer(Class<? extends ConfigContainer<?>> containerClass) {
		try {
			return ConfigContainer.FACTORY.construct(containerClass).finish();
		} catch (Exception e) {
			throw new PojoWeavingException("Failed to instantiate config container");
		}
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

	public ConfigContainer<T> weave() {
		setupWeavingExtensions();

		assert weavingExtensionsPatchworkFactory != null;

		ConfigEntry<T> rootEntry = this.weaveEntry(
				ActualType.ofClass(pojoClass),
				weavingExtensionsPatchworkFactory.create(),
				ProtoWeavingContext.create(configContainer, pojoAnnotations)
		);

		configContainer.attachTree(rootEntry);

		return configContainer;
	}

	private void setupWeavingExtensions() {
		PatchworkFactory.Builder weavingExtensionsPatchworkFactoryBuilder = PatchworkFactory.builder();

		TweedPojoWeavingExtension.SetupContext setupContext = weavingExtensionsPatchworkFactoryBuilder::registerPart;

		for (TweedPojoWeavingExtension weaver : weavingExtensions) {
			weaver.setup(setupContext);
		}

		weavingExtensionsPatchworkFactory = weavingExtensionsPatchworkFactoryBuilder.build();
	}

	private <U> ConfigEntry<U> weaveEntry(
			ActualType<U> valueType,
			Patchwork extensionsData,
			ProtoWeavingContext protoContext
	) {
		extensionsData = extensionsData.copy();

		runBeforeWeaveHooks(valueType, extensionsData, protoContext);

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
					runAfterWeaveHooks(valueType, configEntry, context);
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
						+ ": No matching weavers found"
		);
	}

	private <U> ConfigEntry<U> weavePseudoEntry(
			WeavingContext parentContext,
			String pseudoEntryName,
			Patchwork extensionsData
	) {
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
					runAfterWeaveHooks(valueType, configEntry, context);
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

	private <U> void runBeforeWeaveHooks(
			ActualType<U> dataClass,
			Patchwork extensionsData,
			ProtoWeavingContext protoContext
	) {
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

	private <U> void runAfterWeaveHooks(ActualType<U> dataClass, ConfigEntry<U> configEntry, WeavingContext context) {
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
}
