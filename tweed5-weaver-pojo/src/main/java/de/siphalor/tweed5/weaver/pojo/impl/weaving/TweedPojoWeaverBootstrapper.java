package de.siphalor.tweed5.weaver.pojo.impl.weaving;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.patchwork.api.PatchworkClassCreator;
import de.siphalor.tweed5.patchwork.impl.PatchworkClass;
import de.siphalor.tweed5.patchwork.impl.PatchworkClassGenerator;
import de.siphalor.tweed5.patchwork.impl.PatchworkClassPart;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeaving;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeaver;
import de.siphalor.tweed5.weaver.pojo.api.weaving.WeavingContext;
import de.siphalor.tweed5.weaver.pojo.api.weaving.postprocess.TweedPojoWeavingPostProcessor;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class that sets up and handles all the bits and bobs for weaving a {@link ConfigContainer} out of a POJO.
 * The POJO must be annotated with {@link PojoWeaving}.
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TweedPojoWeaverBootstrapper<T> {
	private final Class<T> pojoClass;
	private final ConfigContainer<T> configContainer;
	private final Collection<TweedPojoWeaver> weavers;
	private final Collection<TweedPojoWeavingPostProcessor> postProcessors;
	private PatchworkClass<WeavingContext.ExtensionsData> contextExtensionsDataClass;

	public static <T> TweedPojoWeaverBootstrapper<T> create(Class<T> pojoClass) {
		PojoWeaving rootWeavingConfig = expectAnnotation(pojoClass, PojoWeaving.class);

		//noinspection unchecked
		ConfigContainer<T> configContainer = (ConfigContainer<T>) createConfigContainer((Class<? extends ConfigContainer<?>>) rootWeavingConfig.container());

		Collection<TweedPojoWeaver> weavers = loadWeavers(Arrays.asList(rootWeavingConfig.weavers()));
		Collection<TweedPojoWeavingPostProcessor> postProcessors = loadPostProcessors(Arrays.asList(rootWeavingConfig.postProcessors()));

		configContainer.registerExtensions(rootWeavingConfig.extensions());
		configContainer.finishExtensionSetup();

		return new TweedPojoWeaverBootstrapper<>(pojoClass, configContainer, weavers, postProcessors);
	}

	private static Collection<TweedPojoWeaver> loadWeavers(Collection<Class<? extends TweedPojoWeaver>> weaverClasses) {
		return weaverClasses.stream()
				.map(weaverClass -> TweedPojoWeaver.FACTORY.construct(weaverClass).finish())
				.collect(Collectors.toList());
	}

	private static Collection<TweedPojoWeavingPostProcessor> loadPostProcessors(Collection<Class<? extends TweedPojoWeavingPostProcessor>> postProcessorClasses) {
		return postProcessorClasses.stream()
				.map(postProcessorClass -> TweedPojoWeavingPostProcessor.FACTORY.construct(postProcessorClass).finish())
				.collect(Collectors.toList());
	}

	private static ConfigContainer<?> createConfigContainer(Class<? extends ConfigContainer<?>> containerClass) {
		try {
			return ConfigContainer.FACTORY.construct(containerClass).finish();
		} catch (Exception e) {
			throw new PojoWeavingException("Failed to instantiate config container");
		}
	}

	private static <A extends Annotation> A expectAnnotation(Class<?> clazz, Class<A> annotationClass) {
		A annotation = clazz.getAnnotation(annotationClass);
		if (annotation == null) {
			throw new PojoWeavingException("Annotation " + annotationClass.getName() + " must be defined on class " + clazz);
		} else {
			return annotation;
		}
	}

	public ConfigContainer<T> weave() {
		setupWeavers();
		WeavingContext weavingContext = createWeavingContext();

		ConfigEntry<T> rootEntry = this.weaveEntry(ActualType.ofClass(pojoClass), weavingContext);
		configContainer.attachAndSealTree(rootEntry);

		return configContainer;
	}

	private void setupWeavers() {
		Map<Class<?>, RegisteredExtensionDataImpl<?>> registeredExtensions = new HashMap<>();

		TweedPojoWeaver.SetupContext setupContext = new TweedPojoWeaver.SetupContext() {
			@Override
			public <E> RegisteredExtensionData<WeavingContext.ExtensionsData, E> registerWeavingContextExtensionData(
					Class<E> dataClass
			) {
				RegisteredExtensionDataImpl<E> registeredExtension = new RegisteredExtensionDataImpl<>();
				registeredExtensions.put(dataClass, registeredExtension);
				return registeredExtension;
			}
		};

		for (TweedPojoWeaver weaver : weavers) {
			weaver.setup(setupContext);
		}

		PatchworkClassCreator<WeavingContext.ExtensionsData> weavingContextCreator = PatchworkClassCreator.<WeavingContext.ExtensionsData>builder()
				.classPackage(this.getClass().getPackage().getName() + ".generated")
				.classPrefix("WeavingContext$")
				.patchworkInterface(WeavingContext.ExtensionsData.class)
				.build();

		try {
			this.contextExtensionsDataClass = weavingContextCreator.createClass(registeredExtensions.keySet());

			for (PatchworkClassPart part : this.contextExtensionsDataClass.parts()) {
				RegisteredExtensionDataImpl<?> registeredExtension = registeredExtensions.get(part.partInterface());
				registeredExtension.setter(part.fieldSetter());
			}
		} catch (PatchworkClassGenerator.GenerationException e) {
			throw new PojoWeavingException("Failed to create weaving context extensions data");
		}
	}

	private WeavingContext createWeavingContext() {
		try {
			WeavingContext.ExtensionsData extensionsData = (WeavingContext.ExtensionsData) contextExtensionsDataClass.constructor().invoke();

			return WeavingContext.builder(this::weaveEntry, configContainer)
					.extensionsData(extensionsData)
					.annotations(pojoClass)
					.build();
		} catch (Throwable e) {
			throw new PojoWeavingException("Failed to create weaving context's extension data");
		}
	}

	private <U> ConfigEntry<U> weaveEntry(ActualType<U> dataClass, WeavingContext context) {
		for (TweedPojoWeaver weaver : weavers) {
			ConfigEntry<U> configEntry = weaver.weaveEntry(dataClass, context);
			if (configEntry != null) {
				if (!configEntry.sealed()) {
					configEntry.seal(configContainer);
				}
				applyPostProcessors(configEntry, context);
				return configEntry;
			}
		}

		throw new PojoWeavingException("Failed to weave " + dataClass + ": No matching weavers found");
	}

	private void applyPostProcessors(ConfigEntry<?> configEntry, WeavingContext context) {
		for (TweedPojoWeavingPostProcessor postProcessor : postProcessors) {
			try {
				postProcessor.apply(configEntry, context);
			} catch (Exception e) {
				log.error("Failed to apply Tweed POJO weaver post processor", e);
			}
		}
	}

	@Setter
	private static class RegisteredExtensionDataImpl<E> implements RegisteredExtensionData<WeavingContext.ExtensionsData, E> {
		private MethodHandle setter;

		@Override
		public void set(WeavingContext.ExtensionsData patchwork, E extension) {
			try {
				setter.invokeWithArguments(patchwork, extension);
			} catch (Throwable e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
